package com.fooddelivery.model;

import com.fooddelivery.model.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    private String rejectionReason;

    @CreationTimestamp
    private LocalDateTime placedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime preparedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Version
    private Long version;

    public Order() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String orderNumber; private User customer;
        private Restaurant restaurant; private OrderStatus status; private BigDecimal totalAmount;
        private String deliveryAddress; private DeliveryPartner deliveryPartner;
        private List<OrderItem> items = new ArrayList<>(); private Payment payment;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderNumber(String orderNumber) { this.orderNumber = orderNumber; return this; }
        public Builder customer(User customer) { this.customer = customer; return this; }
        public Builder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public Builder status(OrderStatus status) { this.status = status; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder deliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; return this; }
        public Builder deliveryPartner(DeliveryPartner dp) { this.deliveryPartner = dp; return this; }
        public Builder payment(Payment payment) { this.payment = payment; return this; }

        public Order build() {
            Order o = new Order();
            o.id = id; o.orderNumber = orderNumber; o.customer = customer;
            o.restaurant = restaurant; o.status = status; o.totalAmount = totalAmount;
            o.deliveryAddress = deliveryAddress; o.deliveryPartner = deliveryPartner;
            o.items = items; o.payment = payment;
            return o;
        }
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public User getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public DeliveryPartner getDeliveryPartner() { return deliveryPartner; }
    public List<OrderItem> getItems() { return items; }
    public Payment getPayment() { return payment; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public LocalDateTime getPreparedAt() { return preparedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public Long getVersion() { return version; }

    public void setId(Long id) { this.id = id; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setDeliveryPartner(DeliveryPartner dp) { this.deliveryPartner = dp; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public void setPreparedAt(LocalDateTime preparedAt) { this.preparedAt = preparedAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}
