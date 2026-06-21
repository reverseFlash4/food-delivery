package com.fooddelivery.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends AppException {
    public InsufficientStockException(String itemName, int requested, int available) {
        super(String.format("Insufficient stock for '%s': requested %d, available %d",
                itemName, requested, available), HttpStatus.CONFLICT);
    }
}
