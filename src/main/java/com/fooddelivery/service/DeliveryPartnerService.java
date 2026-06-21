package com.fooddelivery.service;

import com.fooddelivery.constants.AppConstants;
import com.fooddelivery.dto.request.CreateDeliveryPartnerRequest;
import com.fooddelivery.dto.response.DeliveryPartnerResponse;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.City;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.User;
import com.fooddelivery.model.enums.OrderStatus;
import com.fooddelivery.model.enums.PartnerAvailability;
import com.fooddelivery.model.enums.UserRole;
import com.fooddelivery.repository.CityRepository;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final OrderRepository orderRepository;

    public DeliveryPartnerService(DeliveryPartnerRepository partnerRepository,
                                  UserRepository userRepository,
                                  CityRepository cityRepository,
                                  OrderRepository orderRepository) {
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public DeliveryPartnerResponse registerPartner(CreateDeliveryPartnerRequest req) {
        User user = userRepository.findByIdAndRole(req.getUserId(), UserRole.DELIVERY_PARTNER)
                .orElseThrow(() -> new AppException("User not found or not a delivery partner role", HttpStatus.BAD_REQUEST));
        if (partnerRepository.existsByUser(user)) {
            throw new AppException("Delivery partner profile already exists for this user", HttpStatus.CONFLICT);
        }
        City city = cityRepository.findById(req.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", req.getCityId()));

        DeliveryPartner partner = DeliveryPartner.builder()
                .user(user).city(city).vehicleType(req.getVehicleType())
                .availability(PartnerAvailability.OFFLINE)
                .build();
        return DeliveryPartnerResponse.from(partnerRepository.save(partner));
    }

    @Transactional
    public DeliveryPartnerResponse updateAvailability(Long userId, PartnerAvailability availability) {
        DeliveryPartner partner = getPartnerByUserId(userId);
        partner.setAvailability(availability);
        return DeliveryPartnerResponse.from(partnerRepository.save(partner));
    }

    @Transactional(readOnly = true)
    public DeliveryPartnerResponse getPartnerProfile(Long userId) {
        return DeliveryPartnerResponse.from(getPartnerByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveOrders(Long userId) {
        DeliveryPartner partner = getPartnerByUserId(userId);
        return orderRepository.findByDeliveryPartnerIdAndStatusIn(
                partner.getId(),
                List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.ACCEPTED, OrderStatus.PREPARING)
        ).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryPartnerResponse> getPartnersByCity(Long cityId) {
        return partnerRepository.findByCityIdAndActiveTrue(cityId)
                .stream().map(DeliveryPartnerResponse::from).toList();
    }

    @Transactional
    public DeliveryPartnerResponse updateCity(Long userId, Long cityId) {
        DeliveryPartner partner = getPartnerByUserId(userId);
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City", cityId));
        partner.setCity(city);
        return DeliveryPartnerResponse.from(partnerRepository.save(partner));
    }

    DeliveryPartner getPartnerByUserId(Long userId) {
        return partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(AppConstants.PARTNER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
