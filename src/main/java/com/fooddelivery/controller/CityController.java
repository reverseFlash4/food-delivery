package com.fooddelivery.controller;

import com.fooddelivery.dto.request.CreateCityRequest;
import com.fooddelivery.dto.response.ApiResponse;
import com.fooddelivery.dto.response.CityResponse;
import com.fooddelivery.service.CityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> createCity(@Valid @RequestBody CreateCityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("City created", cityService.createCity(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities() {
        return ResponseEntity.ok(ApiResponse.success(cityService.getAllActiveCities()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(cityService.getCityById(id)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> activateCity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(cityService.toggleCityStatus(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> deactivateCity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(cityService.toggleCityStatus(id, false)));
    }
}
