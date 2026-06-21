package com.fooddelivery.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private boolean available = true;

    private Integer stockQuantity;

    @Version
    private Long version;

    public MenuItem() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String description;
        private BigDecimal price; private String category; private Restaurant restaurant;
        private boolean available = true; private Integer stockQuantity;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder restaurant(Restaurant restaurant) { this.restaurant = restaurant; return this; }
        public Builder available(boolean available) { this.available = available; return this; }
        public Builder stockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; return this; }

        public MenuItem build() {
            MenuItem m = new MenuItem();
            m.id = id; m.name = name; m.description = description; m.price = price;
            m.category = category; m.restaurant = restaurant; m.available = available;
            m.stockQuantity = stockQuantity;
            return m;
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public Restaurant getRestaurant() { return restaurant; }
    public boolean isAvailable() { return available; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Long getVersion() { return version; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
