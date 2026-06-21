package com.fooddelivery.dto.response;

import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.enums.PartnerAvailability;

public class DeliveryPartnerResponse {
    private Long id; private Long userId; private String name; private String email;
    private String phone; private Long cityId; private String cityName;
    private String vehicleType; private PartnerAvailability availability;
    private Double avgRating; private Integer totalRatings; private boolean active;

    public DeliveryPartnerResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private Long userId; private String name; private String email;
        private String phone; private Long cityId; private String cityName;
        private String vehicleType; private PartnerAvailability availability;
        private Double avgRating; private Integer totalRatings; private boolean active;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder cityId(Long cityId) { this.cityId = cityId; return this; }
        public Builder cityName(String cityName) { this.cityName = cityName; return this; }
        public Builder vehicleType(String vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder availability(PartnerAvailability availability) { this.availability = availability; return this; }
        public Builder avgRating(Double avgRating) { this.avgRating = avgRating; return this; }
        public Builder totalRatings(Integer totalRatings) { this.totalRatings = totalRatings; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public DeliveryPartnerResponse build() {
            DeliveryPartnerResponse r = new DeliveryPartnerResponse();
            r.id = id; r.userId = userId; r.name = name; r.email = email; r.phone = phone;
            r.cityId = cityId; r.cityName = cityName; r.vehicleType = vehicleType;
            r.availability = availability; r.avgRating = avgRating;
            r.totalRatings = totalRatings; r.active = active;
            return r;
        }
    }

    public static DeliveryPartnerResponse from(DeliveryPartner dp) {
        return builder().id(dp.getId()).userId(dp.getUser().getId()).name(dp.getUser().getName())
                .email(dp.getUser().getEmail()).phone(dp.getUser().getPhone())
                .cityId(dp.getCity().getId()).cityName(dp.getCity().getName())
                .vehicleType(dp.getVehicleType()).availability(dp.getAvailability())
                .avgRating(dp.getAvgRating()).totalRatings(dp.getTotalRatings())
                .active(dp.isActive()).build();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Long getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public String getVehicleType() { return vehicleType; }
    public PartnerAvailability getAvailability() { return availability; }
    public Double getAvgRating() { return avgRating; }
    public Integer getTotalRatings() { return totalRatings; }
    public boolean isActive() { return active; }
}
