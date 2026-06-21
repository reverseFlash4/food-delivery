package com.fooddelivery.service;

import com.fooddelivery.dto.request.CreateCityRequest;
import com.fooddelivery.dto.response.CityResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.City;
import com.fooddelivery.repository.CityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Transactional
    public CityResponse createCity(CreateCityRequest req) {
        if (cityRepository.existsByNameIgnoreCase(req.getName())) {
            throw new AppException("City already exists: " + req.getName(), HttpStatus.CONFLICT);
        }
        City city = City.builder().name(req.getName()).state(req.getState()).build();
        return CityResponse.from(cityRepository.save(city));
    }

    @Transactional(readOnly = true)
    public List<CityResponse> getAllActiveCities() {
        return cityRepository.findByActiveTrue().stream().map(CityResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CityResponse getCityById(Long id) {
        return CityResponse.from(cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id)));
    }

    @Transactional
    public CityResponse toggleCityStatus(Long id, boolean active) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));
        city.setActive(active);
        return CityResponse.from(cityRepository.save(city));
    }
}
