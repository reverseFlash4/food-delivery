package com.fooddelivery.repository;

import com.fooddelivery.model.Rating;
import com.fooddelivery.model.enums.RatingTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByOrderIdAndTarget(Long orderId, RatingTarget target);

    List<Rating> findByRestaurantId(Long restaurantId);

    List<Rating> findByDeliveryPartnerId(Long partnerId);

    Optional<Rating> findByOrderIdAndTarget(Long orderId, RatingTarget target);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.restaurant.id = :restaurantId")
    Double getAvgRatingForRestaurant(Long restaurantId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.deliveryPartner.id = :partnerId")
    Double getAvgRatingForPartner(Long partnerId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.restaurant.id = :restaurantId")
    Integer countRatingsForRestaurant(Long restaurantId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.deliveryPartner.id = :partnerId")
    Integer countRatingsForPartner(Long partnerId);
}
