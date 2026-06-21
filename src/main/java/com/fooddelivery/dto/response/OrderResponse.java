package com.fooddelivery.dto.response;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id; private String orderNumber; private Long customerId; private String customerName;
    private Long restaurantId; private String restaurantName; private OrderStatus status;
    private BigDecimal totalAmount; private String deliveryAddress;
    private Long deliveryPartnerId; private String deliveryPartnerName;
    private String rejectionReason; private List<OrderItemResponse> items;
    private PaymentResponse payment; private LocalDateTime placedAt;
    private LocalDateTime acceptedAt; private LocalDateTime preparedAt;
    private LocalDateTime pickedUpAt; private LocalDateTime deliveredAt;

    public OrderResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String orderNumber; private Long customerId; private String customerName;
        private Long restaurantId; private String restaurantName; private OrderStatus status;
        private BigDecimal totalAmount; private String deliveryAddress;
        private Long deliveryPartnerId; private String deliveryPartnerName;
        private String rejectionReason; private List<OrderItemResponse> items;
        private PaymentResponse payment; private LocalDateTime placedAt;
        private LocalDateTime acceptedAt; private LocalDateTime preparedAt;
        private LocalDateTime pickedUpAt; private LocalDateTime deliveredAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderNumber(String v) { this.orderNumber = v; return this; }
        public Builder customerId(Long v) { this.customerId = v; return this; }
        public Builder customerName(String v) { this.customerName = v; return this; }
        public Builder restaurantId(Long v) { this.restaurantId = v; return this; }
        public Builder restaurantName(String v) { this.restaurantName = v; return this; }
        public Builder status(OrderStatus v) { this.status = v; return this; }
        public Builder totalAmount(BigDecimal v) { this.totalAmount = v; return this; }
        public Builder deliveryAddress(String v) { this.deliveryAddress = v; return this; }
        public Builder deliveryPartnerId(Long v) { this.deliveryPartnerId = v; return this; }
        public Builder deliveryPartnerName(String v) { this.deliveryPartnerName = v; return this; }
        public Builder rejectionReason(String v) { this.rejectionReason = v; return this; }
        public Builder items(List<OrderItemResponse> v) { this.items = v; return this; }
        public Builder payment(PaymentResponse v) { this.payment = v; return this; }
        public Builder placedAt(LocalDateTime v) { this.placedAt = v; return this; }
        public Builder acceptedAt(LocalDateTime v) { this.acceptedAt = v; return this; }
        public Builder preparedAt(LocalDateTime v) { this.preparedAt = v; return this; }
        public Builder pickedUpAt(LocalDateTime v) { this.pickedUpAt = v; return this; }
        public Builder deliveredAt(LocalDateTime v) { this.deliveredAt = v; return this; }

        public OrderResponse build() {
            OrderResponse r = new OrderResponse();
            r.id = id; r.orderNumber = orderNumber; r.customerId = customerId;
            r.customerName = customerName; r.restaurantId = restaurantId;
            r.restaurantName = restaurantName; r.status = status; r.totalAmount = totalAmount;
            r.deliveryAddress = deliveryAddress; r.deliveryPartnerId = deliveryPartnerId;
            r.deliveryPartnerName = deliveryPartnerName; r.rejectionReason = rejectionReason;
            r.items = items; r.payment = payment; r.placedAt = placedAt;
            r.acceptedAt = acceptedAt; r.preparedAt = preparedAt;
            r.pickedUpAt = pickedUpAt; r.deliveredAt = deliveredAt;
            return r;
        }
    }

    public static OrderResponse from(Order order) {
        var b = builder().id(order.getId()).orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId()).customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurant().getId()).restaurantName(order.getRestaurant().getName())
                .status(order.getStatus()).totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress()).rejectionReason(order.getRejectionReason())
                .placedAt(order.getPlacedAt()).acceptedAt(order.getAcceptedAt())
                .preparedAt(order.getPreparedAt()).pickedUpAt(order.getPickedUpAt())
                .deliveredAt(order.getDeliveredAt());

        if (order.getDeliveryPartner() != null) {
            b.deliveryPartnerId(order.getDeliveryPartner().getId())
             .deliveryPartnerName(order.getDeliveryPartner().getUser().getName());
        }
        if (order.getItems() != null) {
            b.items(order.getItems().stream().map(OrderItemResponse::from).toList());
        }
        if (order.getPayment() != null) {
            b.payment(PaymentResponse.from(order.getPayment()));
        }
        return b.build();
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Long getDeliveryPartnerId() { return deliveryPartnerId; }
    public String getDeliveryPartnerName() { return deliveryPartnerName; }
    public String getRejectionReason() { return rejectionReason; }
    public List<OrderItemResponse> getItems() { return items; }
    public PaymentResponse getPayment() { return payment; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public LocalDateTime getPreparedAt() { return preparedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
}
