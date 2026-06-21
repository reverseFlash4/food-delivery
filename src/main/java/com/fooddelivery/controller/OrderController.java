package com.fooddelivery.controller;

import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.request.UpdateOrderStatusRequest;
import com.fooddelivery.dto.response.ApiResponse;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.model.enums.OrderStatus;
import com.fooddelivery.security.UserPrincipal;
import com.fooddelivery.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully",
                        orderService.placeOrder(req, principal.getId())));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderById(orderId, principal.getId())));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getCustomerOrders(principal.getId(), page, size)));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getRestaurantOrders(restaurantId, status, page, size)));
    }

    @PatchMapping("/{orderId}/restaurant-status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateByRestaurant(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        OrderStatus newStatus = OrderStatus.valueOf(req.getStatus().toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateOrderStatusByRestaurant(orderId, newStatus,
                        req.getNote(), principal.getId())));
    }

    @PatchMapping("/{orderId}/delivery-status")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateByPartner(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        OrderStatus newStatus = OrderStatus.valueOf(req.getStatus().toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateOrderStatusByPartner(orderId, newStatus,
                        req.getNote(), principal.getId())));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(orderId, principal.getId())));
    }
}
