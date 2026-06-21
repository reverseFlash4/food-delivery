package com.fooddelivery.model;

import com.fooddelivery.model.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @CreationTimestamp
    private LocalDateTime changedAt;

    public OrderStatusHistory() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Order order; private OrderStatus fromStatus; private OrderStatus toStatus;
        private String note; private User changedBy;

        public Builder order(Order order) { this.order = order; return this; }
        public Builder fromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; return this; }
        public Builder toStatus(OrderStatus toStatus) { this.toStatus = toStatus; return this; }
        public Builder note(String note) { this.note = note; return this; }
        public Builder changedBy(User changedBy) { this.changedBy = changedBy; return this; }

        public OrderStatusHistory build() {
            OrderStatusHistory h = new OrderStatusHistory();
            h.order = order; h.fromStatus = fromStatus; h.toStatus = toStatus;
            h.note = note; h.changedBy = changedBy;
            return h;
        }
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public OrderStatus getFromStatus() { return fromStatus; }
    public OrderStatus getToStatus() { return toStatus; }
    public String getNote() { return note; }
    public User getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
