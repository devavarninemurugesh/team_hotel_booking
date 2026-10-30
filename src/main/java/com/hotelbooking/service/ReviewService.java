package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.util.Ids;

import java.util.List;

public final class ReviewService {
    private final InMemoryDataStore store;
    public ReviewService(InMemoryDataStore store) { this.store = store; }

    public Review addReview(String bookingId, String guestId, int rating, String comment) {
        Booking booking = store.findBooking(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking was not found."));
        if (!booking.getGuestId().equals(guestId)) throw new BusinessRuleException("Only the booking guest can review this stay.");
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessRuleException("A review can only be added after checkout.");
        }
        Review review = new Review(Ids.newId("REV"), bookingId, guestId, booking.getHotelId(), rating, comment);
        store.addReview(review);
        return review;
    }
    public List<Review> getReviewsForHotel(String hotelId) { return store.getReviewsForHotel(hotelId); }
}
