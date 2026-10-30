package com.hotelbooking.model;

import com.hotelbooking.exception.ValidationException;
import java.time.Instant;

public final class Review {
    private final String id;
    private final String bookingId;
    private final String guestId;
    private final String hotelId;
    private final int rating;
    private final String comment;
    private final Instant createdAt = Instant.now();

    public Review(String id, String bookingId, String guestId, String hotelId, int rating, String comment) {
        this.id = User.required(id, "Review ID");
        this.bookingId = User.required(bookingId, "Booking ID");
        this.guestId = User.required(guestId, "Guest ID");
        this.hotelId = User.required(hotelId, "Hotel ID");
        if (rating < 1 || rating > 5) throw new ValidationException("Rating must be between 1 and 5.");
        this.rating = rating;
        this.comment = User.required(comment, "Review comment");
    }
    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getGuestId() { return guestId; }
    public String getHotelId() { return hotelId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
