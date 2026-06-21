package com.fooddelivery.event;

import com.fooddelivery.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService notificationService;

    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Order placed event: {} for customer {}", event.getOrderNumber(), event.getCustomerEmail());
        notificationService.notifyOrderPlaced(
                event.getCustomerEmail(), event.getOrderNumber(), event.getRestaurantName());
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Order status changed: {} -> {} for order {}", event.getOldStatus(),
                event.getNewStatus(), event.getOrderNumber());
        notificationService.notifyStatusChange(
                event.getOrderNumber(), event.getOldStatus(), event.getNewStatus(),
                event.getCustomerEmail(), event.getRestaurantOwnerEmail(), event.getDeliveryPartnerEmail());
    }
}
