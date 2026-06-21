package com.fooddelivery.event;

import org.springframework.context.ApplicationEvent;

public class OrderPlacedEvent extends ApplicationEvent {
    private final Long orderId;
    private final String orderNumber;
    private final String customerEmail;
    private final String restaurantName;

    public OrderPlacedEvent(Object source, Long orderId, String orderNumber,
                            String customerEmail, String restaurantName) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.restaurantName = restaurantName;
    }

    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerEmail() { return customerEmail; }
    public String getRestaurantName() { return restaurantName; }
}
