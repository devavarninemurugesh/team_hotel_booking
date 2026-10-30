package com.hotelbooking.model;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class Booking {
    private final String id;
    private final String guestId;
    private final String hotelId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final BigDecimal totalPrice;
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    public Booking(String id, String guestId, String hotelId, LocalDate checkIn,
                   LocalDate checkOut, BigDecimal totalPrice) {
        this.id = User.required(id, "Booking ID");
        this.guestId = User.required(guestId, "Guest ID");
        this.hotelId = User.required(hotelId, "Hotel ID");
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out date must be after check-in date.");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        if (totalPrice == null || totalPrice.signum() <= 0) throw new ValidationException("Price must be positive.");
        this.totalPrice = totalPrice.setScale(2, java.math.RoundingMode.HALF_UP);
    }
    public String getId() { return id; }
    public String getGuestId() { return guestId; }
    public String getHotelId() { return hotelId; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
    public void confirm() {
        requireStatus(BookingStatus.PENDING_PAYMENT);
        status = BookingStatus.CONFIRMED;
    }
    public void checkIn() {
        requireStatus(BookingStatus.CONFIRMED);
        status = BookingStatus.CHECKED_IN;
    }
    public void checkOut() {
        requireStatus(BookingStatus.CHECKED_IN);
        status = BookingStatus.COMPLETED;
    }
    public void cancel() {
        if (status == BookingStatus.CANCELLED || status == BookingStatus.COMPLETED) {
            throw new BusinessRuleException("This booking can no longer be cancelled.");
        }
        if (status == BookingStatus.CHECKED_IN) throw new BusinessRuleException("An active stay cannot be cancelled.");
        status = BookingStatus.CANCELLED;
    }
    private void requireStatus(BookingStatus expected) {
        if (status != expected) throw new BusinessRuleException(
                "Booking must be " + expected + " but is " + status + ".");
    }
}
