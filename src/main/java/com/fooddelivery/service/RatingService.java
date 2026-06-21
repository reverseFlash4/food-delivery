package com.fooddelivery.service;

import com.fooddelivery.dto.request.RatingRequest;
import com.fooddelivery.dto.response.RatingResponse;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Rating;
import com.fooddelivery.model.User;
import com.fooddelivery.model.enums.OrderStatus;
import com.fooddelivery.model.enums.RatingTarget;
import com.fooddelivery.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository partnerRepository;

    public RatingService(RatingRepository ratingRepository, OrderRepository orderRepository,
                         UserRepository userRepository, RestaurantRepository restaurantRepository,
                         DeliveryPartnerRepository partnerRepository) {
        this.ratingRepository = ratingRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.partnerRepository = partnerRepository;
    }

    @Transactional
    public RatingResponse submitRating(RatingRequest req, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerId));
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", req.getOrderId()));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AppException("You can only rate your own orders", HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException("Can only rate delivered orders", HttpStatus.BAD_REQUEST);
        }
        if (ratingRepository.existsByOrderIdAndTarget(req.getOrderId(), req.getTarget())) {
            throw new AppException("You have already rated this " + req.getTarget().name().toLowerCase()
                    + " for this order", HttpStatus.CONFLICT);
        }

        var ratingBuilder = Rating.builder()
                .order(order).customer(customer).target(req.getTarget())
                .rating(req.getRating()).review(req.getReview());

        if (req.getTarget() == RatingTarget.RESTAURANT) {
            ratingBuilder.restaurant(order.getRestaurant());
            Rating saved = ratingRepository.save(ratingBuilder.build());
            updateRestaurantAvgRating(order.getRestaurant().getId());
            return RatingResponse.from(saved);
        } else {
            if (order.getDeliveryPartner() == null) {
                throw new AppException("No delivery partner assigned to this order", HttpStatus.BAD_REQUEST);
            }
            ratingBuilder.deliveryPartner(order.getDeliveryPartner());
            Rating saved = ratingRepository.save(ratingBuilder.build());
            updatePartnerAvgRating(order.getDeliveryPartner().getId());
            return RatingResponse.from(saved);
        }
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getRestaurantRatings(Long restaurantId) {
        return ratingRepository.findByRestaurantId(restaurantId)
                .stream().map(RatingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getPartnerRatings(Long partnerId) {
        return ratingRepository.findByDeliveryPartnerId(partnerId)
                .stream().map(RatingResponse::from).toList();
    }

    private void updateRestaurantAvgRating(Long restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
        Double avg = ratingRepository.getAvgRatingForRestaurant(restaurantId);
        Integer count = ratingRepository.countRatingsForRestaurant(restaurantId);
        restaurant.setAvgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        restaurant.setTotalRatings(count != null ? count : 0);
        restaurantRepository.save(restaurant);
    }

    private void updatePartnerAvgRating(Long partnerId) {
        var partner = partnerRepository.findById(partnerId).orElseThrow();
        Double avg = ratingRepository.getAvgRatingForPartner(partnerId);
        Integer count = ratingRepository.countRatingsForPartner(partnerId);
        partner.setAvgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        partner.setTotalRatings(count != null ? count : 0);
        partnerRepository.save(partner);
    }
}
