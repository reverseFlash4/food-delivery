package com.fooddelivery.service;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.event.OrderPlacedEvent;
import com.fooddelivery.event.OrderStatusChangedEvent;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.InsufficientStockException;
import com.fooddelivery.exception.InvalidOrderStateException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.*;
import com.fooddelivery.model.enums.*;
import com.fooddelivery.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository,
                        PaymentRepository paymentRepository, DeliveryPartnerRepository partnerRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.paymentRepository = paymentRepository;
        this.partnerRepository = partnerRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Atomically places an order:
     * 1. Acquires pessimistic write locks on each MenuItem to prevent overselling
     * 2. Validates and deducts stock within the same transaction
     * 3. Creates payment record (simulated)
     * 4. Publishes event post-commit for async fan-out notifications
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest req, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerId));
        Restaurant restaurant = restaurantRepository.findById(req.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", req.getRestaurantId()));

        if (!restaurant.isActive()) {
            throw new AppException("Restaurant is not active", HttpStatus.BAD_REQUEST);
        }
        if (!restaurant.isOpen()) {
            throw new AppException("Restaurant is currently closed", HttpStatus.BAD_REQUEST);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : req.getItems()) {
            // Pessimistic write lock prevents concurrent transactions from overselling
            MenuItem menuItem = menuItemRepository.findByIdWithLock(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemReq.getMenuItemId()));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new AppException("Menu item does not belong to this restaurant", HttpStatus.BAD_REQUEST);
            }
            if (!menuItem.isAvailable()) {
                throw new AppException("Menu item is not available: " + menuItem.getName(), HttpStatus.BAD_REQUEST);
            }

            if (menuItem.getStockQuantity() != null) {
                if (menuItem.getStockQuantity() < itemReq.getQuantity()) {
                    throw new InsufficientStockException(menuItem.getName(),
                            itemReq.getQuantity(), menuItem.getStockQuantity());
                }
                menuItem.setStockQuantity(menuItem.getStockQuantity() - itemReq.getQuantity());
                menuItemRepository.save(menuItem);
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .menuItemName(menuItem.getName())
                    .unitPrice(menuItem.getPrice())
                    .quantity(itemReq.getQuantity())
                    .build();
            orderItems.add(orderItem);
            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .restaurant(restaurant)
                .status(OrderStatus.PLACED)
                .totalAmount(total)
                .deliveryAddress(req.getDeliveryAddress())
                .build();
        order = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.getItems().addAll(orderItems);

        // Simulate payment processing atomically within the same transaction
        Payment payment = Payment.builder()
                .order(order)
                .amount(total)
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(req.getPaymentMethod())
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .processedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
        order.setPayment(payment);

        recordStatusHistory(order, null, OrderStatus.PLACED, "Order placed", customer);

        final Order savedOrder = order;
        // Event fires after commit — safe even if notification delivery fails
        eventPublisher.publishEvent(new OrderPlacedEvent(this,
                savedOrder.getId(), savedOrder.getOrderNumber(),
                customer.getEmail(), restaurant.getName()));

        return OrderResponse.from(savedOrder);
    }

    /**
     * Restaurant owner accepts or rejects an order.
     * On ACCEPTED, attempts automatic delivery partner assignment.
     */
    @Transactional
    public OrderResponse updateOrderStatusByRestaurant(Long orderId, OrderStatus newStatus,
                                                        String note, Long ownerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new AppException("Access denied", HttpStatus.FORBIDDEN);
        }

        OrderStatus current = order.getStatus();
        validateRestaurantTransition(current, newStatus);

        OrderStatus old = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.ACCEPTED) {
            order.setAcceptedAt(LocalDateTime.now());
            tryAssignDeliveryPartner(order);
        } else if (newStatus == OrderStatus.REJECTED) {
            order.setRejectionReason(note);
            restoreStock(order);
        } else if (newStatus == OrderStatus.PREPARING) {
            // Already accepted
        } else if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            order.setPreparedAt(LocalDateTime.now());
        }

        User owner = userRepository.findById(ownerId).orElseThrow();
        recordStatusHistory(order, old, newStatus, note, owner);
        order = orderRepository.save(order);

        publishStatusChange(order, old, newStatus);
        return OrderResponse.from(order);
    }

    /**
     * Delivery partner updates order to OUT_FOR_DELIVERY or DELIVERED.
     */
    @Transactional
    public OrderResponse updateOrderStatusByPartner(Long orderId, OrderStatus newStatus,
                                                     String note, Long partnerUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        DeliveryPartner partner = partnerRepository.findByUserId(partnerUserId)
                .orElseThrow(() -> new AppException("Delivery partner profile not found", HttpStatus.NOT_FOUND));

        if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new AppException("This order is not assigned to you", HttpStatus.FORBIDDEN);
        }

        OrderStatus old = order.getStatus();
        validatePartnerTransition(old, newStatus);
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            order.setPickedUpAt(LocalDateTime.now());
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            partner.setAvailability(PartnerAvailability.AVAILABLE);
            partnerRepository.save(partner);
        }

        User partnerUser = userRepository.findById(partnerUserId).orElseThrow();
        recordStatusHistory(order, old, newStatus, note, partnerUser);
        order = orderRepository.save(order);

        publishStatusChange(order, old, newStatus);
        return OrderResponse.from(order);
    }

    /**
     * Customer cancels an order (only when PLACED).
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AppException("Access denied", HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new InvalidOrderStateException("Orders can only be cancelled when in PLACED status");
        }

        OrderStatus old = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);

        // Refund payment
        if (order.getPayment() != null) {
            order.getPayment().setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(order.getPayment());
        }

        User customer = userRepository.findById(customerId).orElseThrow();
        recordStatusHistory(order, old, OrderStatus.CANCELLED, "Cancelled by customer", customer);
        order = orderRepository.save(order);

        publishStatusChange(order, old, OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        User user = userRepository.findById(userId).orElseThrow();
        assertOrderAccess(order, user);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getCustomerOrders(Long customerId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("placedAt").descending());
        return PageResponse.from(
                orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId, pageable)
                        .map(OrderResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getRestaurantOrders(Long restaurantId, OrderStatus status,
                                                            int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("placedAt").descending());
        var pageData = (status != null)
                ? orderRepository.findByRestaurantIdAndStatusOrderByPlacedAtDesc(restaurantId, status, pageable)
                : orderRepository.findByRestaurantIdOrderByPlacedAtDesc(restaurantId, pageable);
        return PageResponse.from(pageData.map(OrderResponse::from));
    }

    /**
     * Assigns an available delivery partner from the restaurant's city using optimistic locking.
     * Retries up to 3 times on concurrent assignment contention.
     */
    private void tryAssignDeliveryPartner(Order order) {
        Long cityId = order.getRestaurant().getCity().getId();
        List<DeliveryPartner> available = partnerRepository.findAvailablePartnersInCity(cityId);
        if (available.isEmpty()) {
            log.info("No available delivery partners in city {} for order {}", cityId, order.getOrderNumber());
            return;
        }

        for (DeliveryPartner partner : available) {
            try {
                partner.setAvailability(PartnerAvailability.BUSY);
                partnerRepository.save(partner);
                order.setDeliveryPartner(partner);
                log.info("Assigned partner {} to order {}", partner.getId(), order.getOrderNumber());
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Partner {} was concurrently modified, trying next", partner.getId());
            }
        }
        log.warn("Could not assign any partner for order {} due to concurrent assignment", order.getOrderNumber());
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            MenuItem menuItem = item.getMenuItem();
            if (menuItem.getStockQuantity() != null) {
                menuItem.setStockQuantity(menuItem.getStockQuantity() + item.getQuantity());
                menuItemRepository.save(menuItem);
            }
        }
    }

    private void recordStatusHistory(Order order, OrderStatus from, OrderStatus to,
                                     String note, User changedBy) {
        statusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(from)
                .toStatus(to)
                .note(note)
                .changedBy(changedBy)
                .build());
    }

    private void publishStatusChange(Order order, OrderStatus old, OrderStatus newStatus) {
        String partnerEmail = order.getDeliveryPartner() != null
                ? order.getDeliveryPartner().getUser().getEmail() : null;
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this,
                order.getId(), order.getOrderNumber(), old, newStatus,
                order.getCustomer().getEmail(),
                order.getRestaurant().getOwner().getEmail(),
                partnerEmail));
    }

    private void validateRestaurantTransition(OrderStatus current, OrderStatus target) {
        boolean valid = switch (current) {
            case PLACED -> target == OrderStatus.ACCEPTED || target == OrderStatus.REJECTED;
            case ACCEPTED -> target == OrderStatus.PREPARING;
            case PREPARING -> target == OrderStatus.READY_FOR_PICKUP;
            default -> false;
        };
        if (!valid) throw new InvalidOrderStateException(current, target);
    }

    private void validatePartnerTransition(OrderStatus current, OrderStatus target) {
        boolean valid = switch (current) {
            case READY_FOR_PICKUP -> target == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> target == OrderStatus.DELIVERED;
            default -> false;
        };
        if (!valid) throw new InvalidOrderStateException(current, target);
    }

    private void assertOrderAccess(Order order, User user) {
        boolean hasAccess = switch (user.getRole()) {
            case ADMIN -> true;
            case CUSTOMER -> order.getCustomer().getId().equals(user.getId());
            case RESTAURANT_OWNER -> order.getRestaurant().getOwner().getId().equals(user.getId());
            case DELIVERY_PARTNER -> order.getDeliveryPartner() != null &&
                    order.getDeliveryPartner().getUser().getId().equals(user.getId());
        };
        if (!hasAccess) throw new AppException("Access denied", HttpStatus.FORBIDDEN);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
