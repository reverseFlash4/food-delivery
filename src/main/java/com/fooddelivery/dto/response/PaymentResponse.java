package com.fooddelivery.dto.response;

import com.fooddelivery.model.Payment;
import com.fooddelivery.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id; private BigDecimal amount; private PaymentStatus status;
    private String paymentMethod; private String transactionId; private LocalDateTime processedAt;

    public PaymentResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private BigDecimal amount; private PaymentStatus status;
        private String paymentMethod; private String transactionId; private LocalDateTime processedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder status(PaymentStatus status) { this.status = status; return this; }
        public Builder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }

        public PaymentResponse build() {
            PaymentResponse r = new PaymentResponse();
            r.id = id; r.amount = amount; r.status = status;
            r.paymentMethod = paymentMethod; r.transactionId = transactionId; r.processedAt = processedAt;
            return r;
        }
    }

    public static PaymentResponse from(Payment p) {
        return builder().id(p.getId()).amount(p.getAmount()).status(p.getStatus())
                .paymentMethod(p.getPaymentMethod()).transactionId(p.getTransactionId())
                .processedAt(p.getProcessedAt()).build();
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
