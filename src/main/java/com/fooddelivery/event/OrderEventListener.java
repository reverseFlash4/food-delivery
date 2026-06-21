package com.fooddelivery.event;

import com.fooddelivery.constants.AppConstants;
import com.fooddelivery.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {

    private final NotificationService notificationService;

    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async(AppConstants.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        notificationService.notifyOrderPlaced(
                event.getCustomerEmail(), event.getOrderNumber(), event.getRestaurantName());
    }

    @Async(AppConstants.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        notificationService.notifyStatusChange(
                event.getOrderNumber(), event.getOldStatus(), event.getNewStatus(),
                event.getCustomerEmail(), event.getRestaurantOwnerEmail(), event.getDeliveryPartnerEmail());
    }
}
