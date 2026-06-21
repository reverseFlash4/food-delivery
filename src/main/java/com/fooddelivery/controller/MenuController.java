package com.fooddelivery.controller;

import com.fooddelivery.dto.request.MenuItemRequest;
import com.fooddelivery.dto.response.ApiResponse;
import com.fooddelivery.dto.response.MenuItemResponse;
import com.fooddelivery.security.UserPrincipal;
import com.fooddelivery.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getMenu(restaurantId)));
    }

    @GetMapping("/full")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getFullMenu(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getFullMenu(restaurantId, principal.getId())));
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item added",
                        menuService.addMenuItem(restaurantId, req, principal.getId())));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                menuService.updateMenuItem(restaurantId, itemId, req, principal.getId())));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        menuService.deleteMenuItem(restaurantId, itemId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Menu item removed", null));
    }
}
