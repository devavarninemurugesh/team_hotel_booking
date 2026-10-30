package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.exception.ValidationException;
import com.hotelbooking.model.*;
import com.hotelbooking.payment.PaymentMethod;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.util.Ids;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public final class PaymentService {
    private final InMemoryDataStore store;
    public PaymentService(InMemoryDataStore store) { this.store = store; }

    public Payment processPayment(String bookingId, PaymentMethod method, BigDecimal amount) {
        if (method == null) throw new ValidationException("Payment method is required.");
        Booking booking = store.findBooking(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking was not found."));
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Only a booking awaiting payment can be paid.");
        }
        if (amount == null || amount.compareTo(booking.getTotalPrice()) != 0) {
            throw new BusinessRuleException("Payment amount must exactly match the booking total.");
        }
        if (!method.authorize(amount)) throw new BusinessRuleException("Payment authorization was declined.");
        PaymentStatus status = method.payLater() ? PaymentStatus.PENDING : PaymentStatus.SUCCEEDED;
        Payment payment = new Payment(Ids.newId("PAY"), bookingId, method.getType(), amount, status);
        store.addPayment(payment);
        booking.confirm();
        return payment;
    }

    public Optional<Payment> refundPayment(String bookingId, BigDecimal amount) {
        store.findBooking(bookingId).orElseThrow(() -> new NotFoundException("Booking was not found."));
        List<Payment> bookingPayments = store.getPaymentsForBooking(bookingId);
        List<Payment> capturedPayments = bookingPayments.stream()
                .filter(p -> !p.isRefund())
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED || p.getStatus() == PaymentStatus.REFUNDED)
                .toList();
        if (capturedPayments.isEmpty()) return Optional.empty();
        if (amount == null || amount.signum() < 0) throw new ValidationException("Refund amount cannot be negative.");
        BigDecimal captured = capturedPayments.stream().map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alreadyRefunded = bookingPayments.stream().filter(Payment::isRefund)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = captured.subtract(alreadyRefunded);
        if (amount.compareTo(remaining) > 0) throw new BusinessRuleException("Refund exceeds the remaining captured amount.");
        if (amount.signum() == 0) return Optional.empty();
        if (amount.compareTo(remaining) == 0) {
            capturedPayments.stream().filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
                    .forEach(Payment::markRefunded);
        }
        Payment refund = new Payment(Ids.newId("REF"), bookingId,
                capturedPayments.get(0).getMethod(), amount, PaymentStatus.REFUNDED, true);
        store.addPayment(refund);
        return Optional.of(refund);
    }

    public Optional<Payment> settleAtHotel(String bookingId) {
        Payment payment = store.getPaymentsForBooking(bookingId).stream()
                .filter(p -> p.getMethod() == com.hotelbooking.payment.PaymentMethodType.PAY_AT_HOTEL)
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst().orElse(null);
        if (payment == null) return Optional.empty();
        payment.markSucceeded();
        return Optional.of(payment);
    }

    public BigDecimal refundableAmount(String bookingId) {
        List<Payment> bookingPayments = store.getPaymentsForBooking(bookingId);
        BigDecimal captured = bookingPayments.stream()
                .filter(p -> !p.isRefund())
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED || p.getStatus() == PaymentStatus.REFUNDED)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refunded = bookingPayments.stream().filter(Payment::isRefund)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return captured.subtract(refunded).max(BigDecimal.ZERO);
    }
}
