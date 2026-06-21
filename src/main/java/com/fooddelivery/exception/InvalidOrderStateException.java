package com.fooddelivery.exception;

import com.fooddelivery.model.enums.OrderStatus;
import org.springframework.http.HttpStatus;

public class InvalidOrderStateException extends AppException {
    public InvalidOrderStateException(OrderStatus current, OrderStatus target) {
        super(String.format("Cannot transition order from %s to %s", current, target), HttpStatus.BAD_REQUEST);
    }

    public InvalidOrderStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
