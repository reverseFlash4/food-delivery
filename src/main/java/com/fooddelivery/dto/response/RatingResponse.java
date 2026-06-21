package com.fooddelivery.dto.response;

import com.fooddelivery.model.Rating;
import com.fooddelivery.model.enums.RatingTarget;

import java.time.LocalDateTime;

public class RatingResponse {
    private Long id; private Long orderId; private String orderNumber;
    private Long customerId; private String customerName; private RatingTarget target;
    private Long targetId; private String targetName; private Integer rating;
    private String review; private LocalDateTime createdAt;

    public RatingResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private Long orderId; private String orderNumber;
        private Long customerId; private String customerName; private RatingTarget target;
        private Long targetId; private String targetName; private Integer rating;
        private String review; private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder orderNumber(String orderNumber) { this.orderNumber = orderNumber; return this; }
        public Builder customerId(Long customerId) { this.customerId = customerId; return this; }
        public Builder customerName(String customerName) { this.customerName = customerName; return this; }
        public Builder target(RatingTarget target) { this.target = target; return this; }
        public Builder targetId(Long targetId) { this.targetId = targetId; return this; }
        public Builder targetName(String targetName) { this.targetName = targetName; return this; }
        public Builder rating(Integer rating) { this.rating = rating; return this; }
        public Builder review(String review) { this.review = review; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public RatingResponse build() {
            RatingResponse r = new RatingResponse();
            r.id = id; r.orderId = orderId; r.orderNumber = orderNumber;
            r.customerId = customerId; r.customerName = customerName; r.target = target;
            r.targetId = targetId; r.targetName = targetName; r.rating = rating;
            r.review = review; r.createdAt = createdAt;
            return r;
        }
    }

    public static RatingResponse from(Rating r) {
        var b = builder().id(r.getId()).orderId(r.getOrder().getId())
                .orderNumber(r.getOrder().getOrderNumber()).customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getName()).target(r.getTarget())
                .rating(r.getRating()).review(r.getReview()).createdAt(r.getCreatedAt());

        if (r.getTarget() == RatingTarget.RESTAURANT && r.getRestaurant() != null) {
            b.targetId(r.getRestaurant().getId()).targetName(r.getRestaurant().getName());
        } else if (r.getTarget() == RatingTarget.DELIVERY_PARTNER && r.getDeliveryPartner() != null) {
            b.targetId(r.getDeliveryPartner().getId())
             .targetName(r.getDeliveryPartner().getUser().getName());
        }
        return b.build();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public RatingTarget getTarget() { return target; }
    public Long getTargetId() { return targetId; }
    public String getTargetName() { return targetName; }
    public Integer getRating() { return rating; }
    public String getReview() { return review; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
