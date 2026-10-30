package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.exception.ValidationException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.InMemoryDataStore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

public final class BookingService {
    private final InMemoryDataStore store;
    private final SearchService searchService;
    private final PricingService pricingService;
    private final PaymentService paymentService;

    public BookingService(InMemoryDataStore store, SearchService searchService,
                          PricingService pricingService, PaymentService paymentService) {
        this.store = store;
        this.searchService = searchService;
        this.pricingService = pricingService;
        this.paymentService = paymentService;
    }

    public Booking createBooking(String guestId, String hotelId, List<String> roomIds,
                                 LocalDate checkIn, LocalDate checkOut, int guestCount) {
        User user = store.findUser(guestId).orElseThrow(() -> new NotFoundException("Guest was not found."));
        if (!(user instanceof Guest)) throw new BusinessRuleException("Only a guest can create a booking.");
        store.findHotel(hotelId).orElseThrow(() -> new NotFoundException("Hotel was not found."));
        if (roomIds == null || roomIds.isEmpty() || new HashSet<>(roomIds).size() != roomIds.size()) {
            throw new ValidationException("Select one or more distinct rooms.");
        }
        if (guestCount < 1) throw new ValidationException("Guest count must be at least one.");

        int capacity = 0;
        for (String roomId : roomIds) {
            Room room = store.findRoom(roomId).orElseThrow(() -> new NotFoundException("Room was not found."));
            if (!room.getHotelId().equals(hotelId)) throw new BusinessRuleException("Every selected room must belong to this hotel.");
            if (!searchService.isRoomAvailable(roomId, checkIn, checkOut)) {
                throw new BusinessRuleException("Room " + room.getRoomNumber() + " is unavailable for those dates.");
            }
            capacity = Math.addExact(capacity, room.getCapacity());
        }
        if (capacity < guestCount) throw new BusinessRuleException("Selected rooms do not have enough guest capacity.");

        BigDecimal total = pricingService.calculateBookingPrice(roomIds, checkIn, checkOut);
        Booking booking = new Booking("BKG-" + java.util.UUID.randomUUID(), guestId, hotelId,
                checkIn, checkOut, total);
        store.addBooking(booking);
        roomIds.forEach(roomId -> store.addBookingRoom(new BookingRoom(booking.getId(), roomId)));
        return booking;
    }

    public BigDecimal cancelBooking(String bookingId, boolean initiatedByHotel) {
        Booking booking = store.findBooking(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking was not found."));
        if (!initiatedByHotel && !booking.getCheckIn().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("A guest cancellation must be made before the check-in date.");
        }
        BigDecimal requestedRefund = initiatedByHotel ? booking.getTotalPrice() : cancellationRefund(booking);
        BigDecimal refund = requestedRefund.min(paymentService.refundableAmount(bookingId));
        booking.cancel();
        paymentService.refundPayment(bookingId, refund);
        return refund;
    }

    public BigDecimal cancellationRefund(Booking booking) {
        long hoursUntilCheckIn = ChronoUnit.HOURS.between(
                java.time.LocalDateTime.now(), booking.getCheckIn().atStartOfDay());
        if (hoursUntilCheckIn >= 48) return booking.getTotalPrice();
        return booking.getTotalPrice().multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
    }

    public void checkIn(String bookingId, boolean idVerified) {
        if (!idVerified) throw new BusinessRuleException("Government ID must be verified before check-in.");
        Booking booking = findBooking(bookingId);
        LocalDate today = LocalDate.now();
        if (today.isBefore(booking.getCheckIn()) || !today.isBefore(booking.getCheckOut())) {
            throw new BusinessRuleException("Check-in is only available during the booking dates.");
        }
        booking.checkIn();
    }

    public void checkOut(String bookingId) {
        Booking booking = findBooking(bookingId);
        if (LocalDate.now().isBefore(booking.getCheckIn())) {
            throw new BusinessRuleException("Check-out cannot happen before check-in.");
        }
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new BusinessRuleException("Only a checked-in guest can check out.");
        }
        booking.checkOut();
        paymentService.settleAtHotel(bookingId);
    }

    public List<Booking> getBookingsForGuest(String guestId) { return store.getBookingsForGuest(guestId); }
    public List<BookingRoom> getRoomsForBooking(String bookingId) { return store.getBookingRooms(bookingId); }
    private Booking findBooking(String id) {
        return store.findBooking(id).orElseThrow(() -> new NotFoundException("Booking was not found."));
    }
}
