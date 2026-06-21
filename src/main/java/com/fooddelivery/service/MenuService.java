package com.fooddelivery.service;

import com.fooddelivery.constants.AppConstants;
import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public MenuService(MenuItemRepository menuItemRepository,
                       RestaurantRepository restaurantRepository,
                       UserRepository userRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest req, Long ownerId) {
        Restaurant restaurant = getOwnerRestaurant(restaurantId, ownerId);
        MenuItem item = MenuItem.builder()
                .name(req.getName()).description(req.getDescription())
                .price(req.getPrice()).category(req.getCategory())
                .restaurant(restaurant).available(req.isAvailable())
                .stockQuantity(req.getStockQuantity())
                .build();
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long restaurantId, Long itemId,
                                           MenuItemRequest req, Long ownerId) {
        getOwnerRestaurant(restaurantId, ownerId);
        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setCategory(req.getCategory());
        item.setAvailable(req.isAvailable());
        item.setStockQuantity(req.getStockQuantity());
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long restaurantId, Long itemId, Long ownerId) {
        getOwnerRestaurant(restaurantId, ownerId);
        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        item.setAvailable(false);
        menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenu(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", restaurantId);
        }
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream().map(MenuItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getFullMenu(Long restaurantId, Long ownerId) {
        getOwnerRestaurant(restaurantId, ownerId);
        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream().map(MenuItemResponse::from).toList();
    }

    private Restaurant getOwnerRestaurant(Long restaurantId, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));
        return restaurantRepository.findByIdAndOwner(restaurantId, owner)
                .orElseThrow(() -> new AppException(AppConstants.RESTAURANT_ACCESS_DENIED, HttpStatus.FORBIDDEN));
    }
}
