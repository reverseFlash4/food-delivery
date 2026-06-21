package com.fooddelivery.service;

import com.fooddelivery.constants.AppConstants;
import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.RestaurantResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.City;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.model.enums.UserRole;
import com.fooddelivery.repository.CityRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             CityRepository cityRepository,
                             UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest req, Long ownerId) {
        User owner = userRepository.findByIdAndRole(ownerId, UserRole.RESTAURANT_OWNER)
                .orElseThrow(() -> new AppException("User is not a restaurant owner", HttpStatus.FORBIDDEN));
        City city = cityRepository.findById(req.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", req.getCityId()));
        if (!city.isActive()) {
            throw new AppException("City is not active", HttpStatus.BAD_REQUEST);
        }

        Restaurant restaurant = Restaurant.builder()
                .name(req.getName()).address(req.getAddress()).phone(req.getPhone())
                .city(city).owner(owner).cuisineType(req.getCuisineType())
                .build();
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> getRestaurantsByCity(Long cityId, boolean openOnly,
                                                                  int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        var pageData = openOnly
                ? restaurantRepository.findByCityIdAndActiveTrueAndOpenTrue(cityId, pageable)
                : restaurantRepository.findByCityIdAndActiveTrue(cityId, pageable);
        return PageResponse.from(pageData.map(RestaurantResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> searchRestaurants(Long cityId, String name, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return PageResponse.from(
                restaurantRepository.findByCityIdAndActiveTrueAndNameContainingIgnoreCase(cityId, name, pageable)
                        .map(RestaurantResponse::from));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        return RestaurantResponse.from(restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id)));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getMyRestaurants(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));
        return restaurantRepository.findByOwner(owner).stream()
                .map(RestaurantResponse::from).toList();
    }

    @Transactional
    public RestaurantResponse toggleOpenStatus(Long restaurantId, Long ownerId, boolean open) {
        Restaurant restaurant = getOwnerRestaurant(restaurantId, ownerId);
        restaurant.setOpen(open);
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long restaurantId, CreateRestaurantRequest req, Long ownerId) {
        Restaurant restaurant = getOwnerRestaurant(restaurantId, ownerId);
        restaurant.setName(req.getName());
        restaurant.setAddress(req.getAddress());
        restaurant.setPhone(req.getPhone());
        if (req.getCuisineType() != null) restaurant.setCuisineType(req.getCuisineType());
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void deactivateRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
        restaurant.setActive(false);
        restaurant.setOpen(false);
        restaurantRepository.save(restaurant);
    }

    Restaurant getOwnerRestaurant(Long restaurantId, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));
        return restaurantRepository.findByIdAndOwner(restaurantId, owner)
                .orElseThrow(() -> new AppException(AppConstants.RESTAURANT_ACCESS_DENIED, HttpStatus.FORBIDDEN));
    }
}
