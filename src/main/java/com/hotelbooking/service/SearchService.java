package com.hotelbooking.service;

import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.exception.ValidationException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.InMemoryDataStore;

import java.time.LocalDate;
import java.util.List;

public final class SearchService {
    private final InMemoryDataStore store;
    public SearchService(InMemoryDataStore store) { this.store = store; }

    public List<Room> searchAvailableRooms(String city, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        validateDates(checkIn, checkOut);
        if (city == null || city.isBlank()) throw new ValidationException("City cannot be blank.");
        if (guestCount < 1) throw new ValidationException("Guest count must be at least one.");
        return store.getRooms().stream()
                .filter(room -> room.getCapacity() >= guestCount)
                .filter(room -> store.findHotel(room.getHotelId())
                        .map(h -> h.getCity().equalsIgnoreCase(city.trim())).orElse(false))
                .filter(room -> isRoomAvailable(room.getId(), checkIn, checkOut))
                .toList();
    }

    public boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        store.findRoom(roomId).orElseThrow(() -> new NotFoundException("Room was not found."));
        boolean maintenanceConflict = store.getMaintenanceBlocksForRoom(roomId).stream()
                .anyMatch(b -> overlaps(checkIn, checkOut, b.getCheckIn(), b.getCheckOut()));
        if (maintenanceConflict) return false;

        return store.getBookingRooms().stream()
                .filter(br -> br.roomId().equals(roomId))
                .map(br -> store.findBooking(br.bookingId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .noneMatch(booking -> booking.getStatus() != BookingStatus.CANCELLED
                        && overlaps(checkIn, checkOut, booking.getCheckIn(), booking.getCheckOut()));
    }

    public static boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private static void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || checkIn.isBefore(LocalDate.now())
                || !checkOut.isAfter(checkIn)) {
            throw new ValidationException("Choose a check-in date today or later and a later check-out date.");
        }
    }
}
