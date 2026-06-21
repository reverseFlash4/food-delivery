package com.fooddelivery.service;

import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.model.City;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.model.enums.UserRole;
import com.fooddelivery.repository.CityRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock RestaurantRepository restaurantRepository;
    @Mock CityRepository cityRepository;
    @Mock UserRepository userRepository;

    @InjectMocks RestaurantService restaurantService;

    private User owner;
    private City city;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@test.com")
                .role(UserRole.RESTAURANT_OWNER).build();
        city = City.builder().id(1L).name("Bangalore").state("Karnataka").active(true).build();
    }

    @Test
    void createRestaurant_success() {
        var req = new CreateRestaurantRequest();
        req.setName("Biryani Palace");
        req.setAddress("MG Road, Bangalore");
        req.setPhone("9876543210");
        req.setCityId(1L);
        req.setCuisineType("Indian");

        when(userRepository.findByIdAndRole(1L, UserRole.RESTAURANT_OWNER)).thenReturn(Optional.of(owner));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(restaurantRepository.save(any())).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            // Build a new instance with id set (Restaurant has no setId)
            return Restaurant.builder().id(10L).name(r.getName()).address(r.getAddress())
                    .phone(r.getPhone()).city(r.getCity()).owner(r.getOwner())
                    .cuisineType(r.getCuisineType()).build();
        });

        RestaurantResponse response = restaurantService.createRestaurant(req, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Biryani Palace");
        assertThat(response.getCityName()).isEqualTo("Bangalore");
        assertThat(response.isActive()).isTrue();
        assertThat(response.isOpen()).isFalse();
    }

    @Test
    void createRestaurant_inactiveCity_throwsException() {
        city.setActive(false);
        var req = new CreateRestaurantRequest();
        req.setName("Test"); req.setAddress("Addr"); req.setPhone("9876543210"); req.setCityId(1L);

        when(userRepository.findByIdAndRole(1L, UserRole.RESTAURANT_OWNER)).thenReturn(Optional.of(owner));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        assertThatThrownBy(() -> restaurantService.createRestaurant(req, 1L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void createRestaurant_nonOwnerUser_throwsException() {
        when(userRepository.findByIdAndRole(1L, UserRole.RESTAURANT_OWNER)).thenReturn(Optional.empty());

        var req = new CreateRestaurantRequest();
        req.setName("Test"); req.setAddress("Addr"); req.setPhone("9876543210"); req.setCityId(1L);

        assertThatThrownBy(() -> restaurantService.createRestaurant(req, 1L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not a restaurant owner");
    }

    @Test
    void toggleOpenStatus_openRestaurant_success() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Biryani Palace")
                .city(city).owner(owner).active(true).open(false).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        RestaurantResponse response = restaurantService.toggleOpenStatus(1L, 1L, true);

        assertThat(response.isOpen()).isTrue();
    }
}
