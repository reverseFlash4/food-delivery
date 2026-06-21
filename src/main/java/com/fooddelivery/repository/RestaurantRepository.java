package com.fooddelivery.repository;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Page<Restaurant> findByCityIdAndActiveTrueAndOpenTrue(Long cityId, Pageable pageable);
    Page<Restaurant> findByCityIdAndActiveTrue(Long cityId, Pageable pageable);
    List<Restaurant> findByOwner(User owner);
    Optional<Restaurant> findByIdAndOwner(Long id, User owner);
    Page<Restaurant> findByCityIdAndActiveTrueAndNameContainingIgnoreCase(Long cityId, String name, Pageable pageable);
}
