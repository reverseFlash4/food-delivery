package com.fooddelivery.model;

import com.fooddelivery.model.enums.PartnerAvailability;
import jakarta.persistence.*;

@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false)
    private String vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerAvailability availability = PartnerAvailability.OFFLINE;

    private Double avgRating = 0.0;
    private Integer totalRatings = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;

    public DeliveryPartner() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private User user; private City city; private String vehicleType;
        private PartnerAvailability availability = PartnerAvailability.OFFLINE;
        private boolean active = true;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder city(City city) { this.city = city; return this; }
        public Builder vehicleType(String vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder availability(PartnerAvailability availability) { this.availability = availability; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public DeliveryPartner build() {
            DeliveryPartner dp = new DeliveryPartner();
            dp.id = id; dp.user = user; dp.city = city; dp.vehicleType = vehicleType;
            dp.availability = availability; dp.active = active;
            return dp;
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public City getCity() { return city; }
    public String getVehicleType() { return vehicleType; }
    public PartnerAvailability getAvailability() { return availability; }
    public Double getAvgRating() { return avgRating; }
    public Integer getTotalRatings() { return totalRatings; }
    public boolean isActive() { return active; }
    public Long getVersion() { return version; }

    public void setCity(City city) { this.city = city; }
    public void setAvailability(PartnerAvailability availability) { this.availability = availability; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    public void setActive(boolean active) { this.active = active; }
}
