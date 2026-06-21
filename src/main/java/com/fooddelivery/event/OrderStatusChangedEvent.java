package com.fooddelivery.event;

import com.fooddelivery.model.enums.OrderStatus;
import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {
    private final String orderNumber;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    private final String customerEmail;
    private final String restaurantOwnerEmail;
    private final String deliveryPartnerEmail;

    public OrderStatusChangedEvent(Object source, String orderNumber,
                                   OrderStatus oldStatus, OrderStatus newStatus,
                                   String customerEmail, String restaurantOwnerEmail,
                                   String deliveryPartnerEmail) {
        super(source);
        this.orderNumber = orderNumber;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.customerEmail = customerEmail;
        this.restaurantOwnerEmail = restaurantOwnerEmail;
        this.deliveryPartnerEmail = deliveryPartnerEmail;
    }

    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getOldStatus() { return oldStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public String getCustomerEmail() { return customerEmail; }
    public String getRestaurantOwnerEmail() { return restaurantOwnerEmail; }
    public String getDeliveryPartnerEmail() { return deliveryPartnerEmail; }
}
