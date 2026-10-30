package com.hotelbooking.service;

import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.exception.ValidationException;
import com.hotelbooking.model.Room;
import com.hotelbooking.repository.InMemoryDataStore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PricingService {
    private final InMemoryDataStore store;
    public PricingService(InMemoryDataStore store) { this.store = store; }

    public BigDecimal calculateBookingPrice(List<String> roomIds, LocalDate checkIn, LocalDate checkOut) {
        if (roomIds == null || roomIds.isEmpty()) throw new ValidationException("Select at least one room.");
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out date must be after check-in date.");
        }
        Set<String> uniqueIds = new HashSet<>(roomIds);
        if (uniqueIds.size() != roomIds.size()) throw new ValidationException("A room may only be selected once.");
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal perNight = uniqueIds.stream().map(id -> store.findRoom(id)
                .orElseThrow(() -> new NotFoundException("Room was not found: " + id))
                .getNightlyRate()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return perNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }
}
