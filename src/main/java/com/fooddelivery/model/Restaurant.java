package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String cuisineType;
    private Double avgRating = 0.0;
    private Integer totalRatings = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean open = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Restaurant() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String address; private String phone;
        private City city; private User owner; private String cuisineType;
        private Double avgRating = 0.0; private Integer totalRatings = 0;
        private boolean active = true; private boolean open = false;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder city(City city) { this.city = city; return this; }
        public Builder owner(User owner) { this.owner = owner; return this; }
        public Builder cuisineType(String cuisineType) { this.cuisineType = cuisineType; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder open(boolean open) { this.open = open; return this; }

        public Restaurant build() {
            Restaurant r = new Restaurant();
            r.id = id; r.name = name; r.address = address; r.phone = phone;
            r.city = city; r.owner = owner; r.cuisineType = cuisineType;
            r.avgRating = avgRating; r.totalRatings = totalRatings;
            r.active = active; r.open = open;
            return r;
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public City getCity() { return city; }
    public User getOwner() { return owner; }
    public String getCuisineType() { return cuisineType; }
    public Double getAvgRating() { return avgRating; }
    public Integer getTotalRatings() { return totalRatings; }
    public boolean isActive() { return active; }
    public boolean isOpen() { return open; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCity(City city) { this.city = city; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    public void setActive(boolean active) { this.active = active; }
    public void setOpen(boolean open) { this.open = open; }
}
