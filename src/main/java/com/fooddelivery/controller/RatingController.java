package com.fooddelivery.controller;

import com.fooddelivery.dto.request.RatingRequest;
import com.fooddelivery.dto.response.ApiResponse;
import com.fooddelivery.dto.response.RatingResponse;
import com.fooddelivery.security.UserPrincipal;
import com.fooddelivery.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @Valid @RequestBody RatingRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rating submitted",
                        ratingService.submitRating(req, principal.getId())));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRestaurantRatings(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(
                ratingService.getRestaurantRatings(restaurantId)));
    }

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getPartnerRatings(
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.success(
                ratingService.getPartnerRatings(partnerId)));
    }
}
