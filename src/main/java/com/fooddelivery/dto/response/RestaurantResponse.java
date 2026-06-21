package com.fooddelivery.dto.response;

import com.fooddelivery.model.Restaurant;

public class RestaurantResponse {
    private Long id; private String name; private String address; private String phone;
    private String cuisineType; private Long cityId; private String cityName;
    private Long ownerId; private String ownerName; private boolean active; private boolean open;
    private Double avgRating; private Integer totalRatings;

    public RestaurantResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String address; private String phone;
        private String cuisineType; private Long cityId; private String cityName;
        private Long ownerId; private String ownerName; private boolean active; private boolean open;
        private Double avgRating; private Integer totalRatings;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder cuisineType(String cuisineType) { this.cuisineType = cuisineType; return this; }
        public Builder cityId(Long cityId) { this.cityId = cityId; return this; }
        public Builder cityName(String cityName) { this.cityName = cityName; return this; }
        public Builder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public Builder ownerName(String ownerName) { this.ownerName = ownerName; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder open(boolean open) { this.open = open; return this; }
        public Builder avgRating(Double avgRating) { this.avgRating = avgRating; return this; }
        public Builder totalRatings(Integer totalRatings) { this.totalRatings = totalRatings; return this; }

        public RestaurantResponse build() {
            RestaurantResponse r = new RestaurantResponse();
            r.id = id; r.name = name; r.address = address; r.phone = phone;
            r.cuisineType = cuisineType; r.cityId = cityId; r.cityName = cityName;
            r.ownerId = ownerId; r.ownerName = ownerName; r.active = active; r.open = open;
            r.avgRating = avgRating; r.totalRatings = totalRatings;
            return r;
        }
    }

    public static RestaurantResponse from(Restaurant r) {
        return builder().id(r.getId()).name(r.getName()).address(r.getAddress()).phone(r.getPhone())
                .cuisineType(r.getCuisineType()).cityId(r.getCity().getId()).cityName(r.getCity().getName())
                .ownerId(r.getOwner().getId()).ownerName(r.getOwner().getName())
                .active(r.isActive()).open(r.isOpen())
                .avgRating(r.getAvgRating()).totalRatings(r.getTotalRatings()).build();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getCuisineType() { return cuisineType; }
    public Long getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public boolean isActive() { return active; }
    public boolean isOpen() { return open; }
    public Double getAvgRating() { return avgRating; }
    public Integer getTotalRatings() { return totalRatings; }
}
