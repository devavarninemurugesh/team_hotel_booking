package com.hotelbooking.payment;

import com.hotelbooking.exception.ValidationException;
import java.math.BigDecimal;

public final class CardPayment implements PaymentMethod {
    private final String paymentToken;
    public CardPayment(String paymentToken) {
        if (paymentToken == null || paymentToken.trim().length() < 8) {
            throw new ValidationException("A demo payment token of at least 8 characters is required.");
        }
        this.paymentToken = paymentToken.trim();
    }
    public String getPaymentToken() { return paymentToken; }
    @Override public PaymentMethodType getType() { return PaymentMethodType.CARD; }
    @Override public boolean authorize(BigDecimal amount) { return amount != null && amount.signum() > 0; }
}
