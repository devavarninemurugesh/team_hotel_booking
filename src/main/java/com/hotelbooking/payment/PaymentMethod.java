package com.hotelbooking.payment;

import java.math.BigDecimal;

public interface PaymentMethod {
    PaymentMethodType getType();
    boolean authorize(BigDecimal amount);
    default boolean payLater() { return false; }
}
