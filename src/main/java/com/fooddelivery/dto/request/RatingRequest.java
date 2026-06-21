package com.fooddelivery.dto.request;

import com.fooddelivery.model.enums.RatingTarget;
import jakarta.validation.constraints.*;

public class RatingRequest {
    @NotNull private Long orderId;
    @NotNull private RatingTarget target;
    @NotNull @Min(1) @Max(5) private Integer rating;
    @Size(max = 1000) private String review;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public RatingTarget getTarget() { return target; }
    public void setTarget(RatingTarget target) { this.target = target; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}
