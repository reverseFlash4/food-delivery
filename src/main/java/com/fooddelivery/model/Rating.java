package com.fooddelivery.model;

import com.fooddelivery.model.enums.RatingTarget;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "target"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingTarget target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String review;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Rating() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Order order; private User customer; private RatingTarget target;
        private Restaurant restaurant; private DeliveryPartner deliveryPartner;
        private Integer rating; private String review;

        public Builder order(Order order) { this.order = order; return this; }
        public Builder customer(User customer) { this.customer = customer; return this; }
        public Builder target(RatingTarget target) { this.target = target; return this; }
        public Builder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public Builder deliveryPartner(DeliveryPartner dp) { this.deliveryPartner = dp; return this; }
        public Builder rating(Integer rating) { this.rating = rating; return this; }
        public Builder review(String review) { this.review = review; return this; }

        public Rating build() {
            Rating r = new Rating();
            r.order = order; r.customer = customer; r.target = target;
            r.restaurant = restaurant; r.deliveryPartner = deliveryPartner;
            r.rating = rating; r.review = review;
            return r;
        }
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public User getCustomer() { return customer; }
    public RatingTarget getTarget() { return target; }
    public Restaurant getRestaurant() { return restaurant; }
    public DeliveryPartner getDeliveryPartner() { return deliveryPartner; }
    public Integer getRating() { return rating; }
    public String getReview() { return review; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
