package com.fooddelivery.repository;

import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.User;
import com.fooddelivery.model.enums.PartnerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    Optional<DeliveryPartner> findByUser(User user);

    Optional<DeliveryPartner> findByUserId(Long userId);

    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.city.id = :cityId AND dp.availability = 'AVAILABLE' AND dp.active = true")
    List<DeliveryPartner> findAvailablePartnersInCity(Long cityId);

    List<DeliveryPartner> findByCityIdAndActiveTrue(Long cityId);

    boolean existsByUser(User user);

    List<DeliveryPartner> findByAvailabilityAndCityId(PartnerAvailability availability, Long cityId);
}
