package com.fooddelivery.event;

import org.springframework.context.ApplicationEvent;

public class OrderPlacedEvent extends ApplicationEvent {
    private final String orderNumber;
    private final String customerEmail;
    private final String restaurantName;

    public OrderPlacedEvent(Object source, String orderNumber,
                            String customerEmail, String restaurantName) {
        super(source);
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.restaurantName = restaurantName;
    }

    public String getOrderNumber() { return orderNumber; }
    public String getCustomerEmail() { return customerEmail; }
    public String getRestaurantName() { return restaurantName; }
}
