package com.fooddelivery.dto.response;

import com.fooddelivery.model.OrderItem;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Long id; private Long menuItemId; private String menuItemName;
    private BigDecimal unitPrice; private Integer quantity; private BigDecimal subtotal;

    public OrderItemResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private Long menuItemId; private String menuItemName;
        private BigDecimal unitPrice; private Integer quantity; private BigDecimal subtotal;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder menuItemId(Long menuItemId) { this.menuItemId = menuItemId; return this; }
        public Builder menuItemName(String menuItemName) { this.menuItemName = menuItemName; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }

        public OrderItemResponse build() {
            OrderItemResponse r = new OrderItemResponse();
            r.id = id; r.menuItemId = menuItemId; r.menuItemName = menuItemName;
            r.unitPrice = unitPrice; r.quantity = quantity; r.subtotal = subtotal;
            return r;
        }
    }

    public static OrderItemResponse from(OrderItem item) {
        return builder().id(item.getId()).menuItemId(item.getMenuItem().getId())
                .menuItemName(item.getMenuItemName()).unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity()).subtotal(item.getSubtotal()).build();
    }

    public Long getId() { return id; }
    public Long getMenuItemId() { return menuItemId; }
    public String getMenuItemName() { return menuItemName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}
