package com.hotelbooking.model;

import com.hotelbooking.exception.ValidationException;
import com.hotelbooking.payment.PaymentMethodType;
import java.math.BigDecimal;
import java.time.Instant;

public final class Payment {
    private final String id;
    private final String bookingId;
    private final PaymentMethodType method;
    private final BigDecimal amount;
    private final boolean refund;
    private final Instant createdAt = Instant.now();
    private PaymentStatus status;

    public Payment(String id, String bookingId, PaymentMethodType method, BigDecimal amount, PaymentStatus status) {
        this(id, bookingId, method, amount, status, false);
    }

    public Payment(String id, String bookingId, PaymentMethodType method, BigDecimal amount,
                   PaymentStatus status, boolean refund) {
        this.id = User.required(id, "Payment ID");
        this.bookingId = User.required(bookingId, "Booking ID");
        if (method == null || status == null) throw new ValidationException("Payment method and status are required.");
        if (amount == null || amount.signum() <= 0) throw new ValidationException("Payment amount must be positive.");
        this.method = method;
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.status = status;
        this.refund = refund;
    }
    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public PaymentMethodType getMethod() { return method; }
    public BigDecimal getAmount() { return amount; }
    public boolean isRefund() { return refund; }
    public Instant getCreatedAt() { return createdAt; }
    public PaymentStatus getStatus() { return status; }
    public void markSucceeded() {
        if (status != PaymentStatus.PENDING) throw new ValidationException("Only a pending payment can be settled.");
        status = PaymentStatus.SUCCEEDED;
    }
    public void markRefunded() {
        if (status != PaymentStatus.SUCCEEDED) throw new ValidationException("Only a successful payment can be refunded.");
        status = PaymentStatus.REFUNDED;
    }
}
