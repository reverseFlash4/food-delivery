package com.fooddelivery.repository;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByRestaurantIdOrderByPlacedAtDesc(Long restaurantId, Pageable pageable);

    Page<Order> findByRestaurantIdAndStatusOrderByPlacedAtDesc(Long restaurantId, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.restaurant.city.id = :cityId AND o.status = :status AND o.deliveryPartner IS NULL")
    List<Order> findUnassignedOrdersInCity(Long cityId, OrderStatus status);

    List<Order> findByDeliveryPartnerIdAndStatusIn(Long partnerId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.restaurant.id = :restaurantId AND o.status = 'DELIVERED'")
    List<Order> findDeliveredOrdersByCustomerAndRestaurant(Long customerId, Long restaurantId);
}
