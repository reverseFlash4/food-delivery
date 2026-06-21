package com.fooddelivery.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private String menuItemName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    public OrderItem() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private Order order; private MenuItem menuItem;
        private String menuItemName; private BigDecimal unitPrice; private Integer quantity;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder order(Order order) { this.order = order; return this; }
        public Builder menuItem(MenuItem menuItem) { this.menuItem = menuItem; return this; }
        public Builder menuItemName(String menuItemName) { this.menuItemName = menuItemName; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }

        public OrderItem build() {
            OrderItem oi = new OrderItem();
            oi.id = id; oi.order = order; oi.menuItem = menuItem;
            oi.menuItemName = menuItemName; oi.unitPrice = unitPrice; oi.quantity = quantity;
            return oi;
        }
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public MenuItem getMenuItem() { return menuItem; }
    public String getMenuItemName() { return menuItemName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }

    public void setOrder(Order order) { this.order = order; }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
