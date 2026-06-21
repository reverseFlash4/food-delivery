package com.fooddelivery.dto.response;

import com.fooddelivery.model.MenuItem;

import java.math.BigDecimal;

public class MenuItemResponse {
    private Long id; private String name; private String description;
    private BigDecimal price; private String category; private boolean available;
    private Integer stockQuantity; private Long restaurantId;

    public MenuItemResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String description;
        private BigDecimal price; private String category; private boolean available;
        private Integer stockQuantity; private Long restaurantId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder available(boolean available) { this.available = available; return this; }
        public Builder stockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; return this; }
        public Builder restaurantId(Long restaurantId) { this.restaurantId = restaurantId; return this; }

        public MenuItemResponse build() {
            MenuItemResponse r = new MenuItemResponse();
            r.id = id; r.name = name; r.description = description; r.price = price;
            r.category = category; r.available = available;
            r.stockQuantity = stockQuantity; r.restaurantId = restaurantId;
            return r;
        }
    }

    public static MenuItemResponse from(MenuItem m) {
        return builder().id(m.getId()).name(m.getName()).description(m.getDescription())
                .price(m.getPrice()).category(m.getCategory()).available(m.isAvailable())
                .stockQuantity(m.getStockQuantity()).restaurantId(m.getRestaurant().getId()).build();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Long getRestaurantId() { return restaurantId; }
}
