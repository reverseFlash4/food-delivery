package com.fooddelivery.service;

import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.InsufficientStockException;
import com.fooddelivery.model.*;
import com.fooddelivery.model.enums.*;
import com.fooddelivery.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock UserRepository userRepository;
    @Mock RestaurantRepository restaurantRepository;
    @Mock MenuItemRepository menuItemRepository;
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock DeliveryPartnerRepository partnerRepository;
    @Mock OrderStatusHistoryRepository statusHistoryRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks OrderService orderService;

    private User customer;
    private Restaurant restaurant;
    private City city;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        city = City.builder().id(1L).name("Bangalore").state("Karnataka").active(true).build();
        customer = User.builder().id(1L).name("Test Customer").email("customer@test.com")
                .role(UserRole.CUSTOMER).active(true).build();
        User owner = User.builder().id(2L).name("Owner").email("owner@test.com")
                .role(UserRole.RESTAURANT_OWNER).build();
        restaurant = Restaurant.builder().id(1L).name("Test Restaurant")
                .city(city).owner(owner).active(true).open(true).build();
        menuItem = MenuItem.builder().id(1L).name("Burger").price(new BigDecimal("150.00"))
                .restaurant(restaurant).available(true).stockQuantity(10).build();
    }

    @Test
    void placeOrder_success_withStockDeduction() {
        var req = buildPlaceOrderRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            o.setOrderNumber("ORD-TEST01");
            return o;
        });
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(req, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("300.00"); // 2 * 150
        assertThat(menuItem.getStockQuantity()).isEqualTo(8); // 10 - 2 deducted
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void placeOrder_insufficientStock_throwsException() {
        menuItem.setStockQuantity(1);
        var req = buildPlaceOrderRequest(); // requests qty=2

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(menuItem));

        assertThatThrownBy(() -> orderService.placeOrder(req, 1L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Burger");

        // Stock must NOT be deducted on failure
        assertThat(menuItem.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void placeOrder_restaurantClosed_throwsException() {
        restaurant.setOpen(false);
        var req = buildPlaceOrderRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> orderService.placeOrder(req, 1L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void placeOrder_unlimitedStock_doesNotDeductStock() {
        menuItem.setStockQuantity(null); // unlimited
        var req = buildPlaceOrderRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            o.setOrderNumber("ORD-UNLIMITED");
            return o;
        });
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(req, 1L);

        assertThat(response).isNotNull();
        assertThat(menuItem.getStockQuantity()).isNull(); // unchanged
        verify(menuItemRepository, never()).save(any()); // no stock save for unlimited
    }

    @Test
    void cancelOrder_whenPlaced_restoresStock() {
        Order order = buildPlacedOrder();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any())).thenReturn(order);
        when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(100L, 1L);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(menuItem.getStockQuantity()).isEqualTo(12); // 10 + 2 restored
    }

    @Test
    void cancelOrder_whenAlreadyAccepted_throwsException() {
        Order order = buildPlacedOrder();
        order.setStatus(OrderStatus.ACCEPTED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(100L, 1L))
                .isInstanceOf(com.fooddelivery.exception.InvalidOrderStateException.class);
    }

    private PlaceOrderRequest buildPlaceOrderRequest() {
        var itemReq = new OrderItemRequest();
        itemReq.setMenuItemId(1L);
        itemReq.setQuantity(2);

        var req = new PlaceOrderRequest();
        req.setRestaurantId(1L);
        req.setDeliveryAddress("123 Test St");
        req.setPaymentMethod("CARD");
        req.setItems(List.of(itemReq));
        return req;
    }

    private Order buildPlacedOrder() {
        OrderItem item = OrderItem.builder()
                .id(1L).menuItem(menuItem).menuItemName("Burger")
                .unitPrice(new BigDecimal("150.00")).quantity(2).build();
        menuItem.setStockQuantity(10);

        Payment payment = Payment.builder().id(1L)
                .status(PaymentStatus.COMPLETED).amount(new BigDecimal("300.00")).build();

        Order order = Order.builder().id(100L).orderNumber("ORD-TEST01")
                .customer(customer).restaurant(restaurant)
                .status(OrderStatus.PLACED)
                .totalAmount(new BigDecimal("300.00"))
                .deliveryAddress("123 Test St")
                .payment(payment)
                .build();
        order.getItems().add(item);
        item.setOrder(order);
        return order;
    }
}
