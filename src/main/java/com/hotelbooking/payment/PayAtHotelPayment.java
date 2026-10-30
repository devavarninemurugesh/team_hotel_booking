package com.hotelbooking.payment;

import java.math.BigDecimal;

public final class PayAtHotelPayment implements PaymentMethod {
    @Override public PaymentMethodType getType() { return PaymentMethodType.PAY_AT_HOTEL; }
    @Override public boolean authorize(BigDecimal amount) { return amount != null && amount.signum() > 0; }
    @Override public boolean payLater() { return true; }
}
