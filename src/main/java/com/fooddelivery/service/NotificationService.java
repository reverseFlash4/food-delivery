package com.fooddelivery.service;

import com.fooddelivery.model.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void notifyOrderPlaced(String customerEmail, String orderNumber, String restaurantName) {
        log.info("[NOTIFY][CUSTOMER] Order {} placed at {} — confirmation sent to {}",
                orderNumber, restaurantName, customerEmail);
        log.info("[NOTIFY][RESTAURANT] New order {} received", orderNumber);
    }

    public void notifyStatusChange(String orderNumber, OrderStatus from, OrderStatus to,
                                   String customerEmail, String restaurantEmail, String partnerEmail) {
        log.info("[NOTIFY][CUSTOMER] Order {} status: {} → {} — sent to {}",
                orderNumber, from, to, customerEmail);
        if (restaurantEmail != null) {
            log.info("[NOTIFY][RESTAURANT] Order {} status: {} → {} — sent to {}",
                    orderNumber, from, to, restaurantEmail);
        }
        if (partnerEmail != null) {
            log.info("[NOTIFY][PARTNER] Order {} status: {} → {} — sent to {}",
                    orderNumber, from, to, partnerEmail);
        }
    }

}
