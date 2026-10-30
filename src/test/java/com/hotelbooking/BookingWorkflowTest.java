package com.hotelbooking;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.exception.DuplicateRecordException;
import com.hotelbooking.model.*;
import com.hotelbooking.payment.UpiPayment;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingWorkflowTest {
    private InMemoryDataStore store;
    private AuthenticationService auth;
    private SearchService search;
    private PricingService pricing;
    private PaymentService payments;
    private BookingService bookings;
    private ReviewService reviews;
    private Guest guest;
    private Hotel hotel;
    private Room room;

    @BeforeEach
    void setUp() {
        store = new InMemoryDataStore();
        auth = new AuthenticationService(store);
        HotelManager manager = auth.createHotelManager("Manager", "manager@example.com", "9876543210", "ManagerPass1");
        guest = auth.registerGuest("Guest", "guest@example.com", "9876543211",
                IdProofType.PASSPORT, "P1234567", "GuestPass1");
        hotel = new HotelService(store).createHotel("Test Stay", "Chennai", "1 Test Road", manager.getId());
        room = new RoomService(store).createRoom(hotel.getId(), "101", RoomType.DOUBLE, 2,
                new BigDecimal("2500.00"));
        search = new SearchService(store);
        pricing = new PricingService(store);
        payments = new PaymentService(store);
        bookings = new BookingService(store, search, pricing, payments);
        reviews = new ReviewService(store);
    }

    @Test
    void completeGuestRegistrationBookingPaymentCheckoutAndReviewWorkflow() {
        LocalDate today = LocalDate.now();
        Booking booking = bookings.createBooking(guest.getId(), hotel.getId(), List.of(room.getId()),
                today, today.plusDays(1), 2);

        assertEquals(new BigDecimal("2500.00"), booking.getTotalPrice());
        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
        Payment payment = payments.processPayment(booking.getId(),
                new UpiPayment("guest@bank"), booking.getTotalPrice());
        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        assertThrows(BusinessRuleException.class, () -> bookings.checkIn(booking.getId(), false));
        bookings.checkIn(booking.getId(), true);
        bookings.checkOut(booking.getId());
        Review review = reviews.addReview(booking.getId(), guest.getId(), 5, "Excellent stay");

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        assertEquals(hotel.getId(), review.getHotelId());
        assertEquals(1, reviews.getReviewsForHotel(hotel.getId()).size());
        assertFalse(search.isRoomAvailable(room.getId(), today, today.plusDays(1)));
        assertTrue(search.isRoomAvailable(room.getId(), today.plusDays(1), today.plusDays(2)));
    }

    @Test
    void rejectsOverlappingBookingWithoutCreatingDuplicateReservation() {
        LocalDate today = LocalDate.now();
        Booking first = bookings.createBooking(guest.getId(), hotel.getId(), List.of(room.getId()),
                today, today.plusDays(2), 1);
        payments.processPayment(first.getId(), new UpiPayment("guest@bank"), first.getTotalPrice());

        assertThrows(BusinessRuleException.class, () -> bookings.createBooking(guest.getId(), hotel.getId(),
                List.of(room.getId()), today.plusDays(1), today.plusDays(3), 1));
        assertEquals(1, store.getBookings().size());
    }

    @Test
    void verifiesPasswordsAndRejectsDuplicateEmail() {
        assertTrue(auth.authenticate("GUEST@example.com", "GuestPass1").isPresent());
        assertTrue(auth.authenticate("guest@example.com", "wrong-pass").isEmpty());
        assertThrows(DuplicateRecordException.class, () -> auth.registerGuest("Other", "guest@example.com",
                "9876543212", IdProofType.AADHAAR, "123456789012", "AnotherPass1"));
    }

    @Test
    void payAtHotelIsSettledAtCheckoutAndRefundIsBoundedByCapturedAmount() {
        LocalDate today = LocalDate.now();
        Booking booking = bookings.createBooking(guest.getId(), hotel.getId(), List.of(room.getId()),
                today, today.plusDays(1), 1);
        payments.processPayment(booking.getId(), new com.hotelbooking.payment.PayAtHotelPayment(),
                booking.getTotalPrice());
        assertEquals(PaymentStatus.PENDING, store.getPaymentsForBooking(booking.getId()).getFirst().getStatus());

        bookings.checkIn(booking.getId(), true);
        bookings.checkOut(booking.getId());
        assertEquals(PaymentStatus.SUCCEEDED, store.getPaymentsForBooking(booking.getId()).getFirst().getStatus());
        assertEquals(booking.getTotalPrice(), payments.refundableAmount(booking.getId()));
    }

    @Test
    void guestCancellationRefundsHalfWhenCheckInIsWithin48Hours() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Booking booking = bookings.createBooking(guest.getId(), hotel.getId(), List.of(room.getId()),
                checkIn, checkIn.plusDays(1), 1);
        payments.processPayment(booking.getId(), new UpiPayment("guest@bank"), booking.getTotalPrice());

        BigDecimal refund = bookings.cancelBooking(booking.getId(), false);

        assertEquals(new BigDecimal("1250.00"), refund);
        assertEquals(new BigDecimal("1250.00"), payments.refundableAmount(booking.getId()));
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(1, store.getPaymentsForBooking(booking.getId()).stream().filter(Payment::isRefund).count());
    }

    @Test
    void duplicateUserIdsAreRejectedBySharedStore() {
        assertThrows(DuplicateRecordException.class, () -> store.addUser(guest, "unused"));
        assertEquals(2, store.getUsers().size());
    }
}
