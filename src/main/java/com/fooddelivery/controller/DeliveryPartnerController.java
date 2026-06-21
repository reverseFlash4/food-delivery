package com.fooddelivery.controller;

import com.fooddelivery.dto.request.CreateDeliveryPartnerRequest;
import com.fooddelivery.dto.response.ApiResponse;
import com.fooddelivery.dto.response.DeliveryPartnerResponse;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.model.enums.PartnerAvailability;
import com.fooddelivery.security.UserPrincipal;
import com.fooddelivery.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-partners")
public class DeliveryPartnerController {

    private final DeliveryPartnerService partnerService;

    public DeliveryPartnerController(DeliveryPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> registerPartner(
            @Valid @RequestBody CreateDeliveryPartnerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery partner registered",
                        partnerService.registerPartner(req)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                partnerService.getPartnerProfile(principal.getId())));
    }

    @PatchMapping("/me/availability")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> updateAvailability(
            @RequestParam PartnerAvailability status,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                partnerService.updateAvailability(principal.getId(), status)));
    }

    @GetMapping("/me/orders")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getActiveOrders(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                partnerService.getActiveOrders(principal.getId())));
    }

    @PatchMapping("/me/city/{cityId}")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> updateCity(
            @PathVariable Long cityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                partnerService.updateCity(principal.getId(), cityId)));
    }

    @GetMapping("/city/{cityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeliveryPartnerResponse>>> getPartnersByCity(
            @PathVariable Long cityId) {
        return ResponseEntity.ok(ApiResponse.success(
                partnerService.getPartnersByCity(cityId)));
    }
}
