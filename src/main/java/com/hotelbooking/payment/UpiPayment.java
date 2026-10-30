package com.hotelbooking.payment;

import com.hotelbooking.exception.ValidationException;
import java.math.BigDecimal;

public final class UpiPayment implements PaymentMethod {
    private final String upiId;
    public UpiPayment(String upiId) {
        if (upiId == null || !upiId.trim().matches("^[A-Za-z0-9._-]{2,}@[A-Za-z0-9.-]{2,}$")) {
            throw new ValidationException("Enter a valid UPI ID (for example, name@bank).");
        }
        this.upiId = upiId.trim();
    }
    public String getUpiId() { return upiId; }
    @Override public PaymentMethodType getType() { return PaymentMethodType.UPI; }
    @Override public boolean authorize(BigDecimal amount) { return amount != null && amount.signum() > 0; }
}
