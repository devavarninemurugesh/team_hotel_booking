package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.model.MaintenanceBlock;
import com.hotelbooking.model.Room;
import com.hotelbooking.model.BookingStatus;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.util.Ids;

import java.time.LocalDate;
import java.util.List;

public final class MaintenanceService {
    private final InMemoryDataStore store;
    public MaintenanceService(InMemoryDataStore store) { this.store = store; }

    public MaintenanceBlock blockRoom(String roomId, LocalDate from, LocalDate until) {
        Room room = store.findRoom(roomId).orElseThrow(() -> new NotFoundException("Room was not found."));
        if (from == null || until == null || from.isBefore(LocalDate.now()) || !until.isAfter(from)) {
            throw new com.hotelbooking.exception.ValidationException("Choose a maintenance period starting today or later.");
        }
        boolean bookingConflict = store.getBookingRooms().stream()
                .filter(br -> br.roomId().equals(roomId))
                .map(br -> store.findBooking(br.bookingId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .anyMatch(b -> b.getStatus() != BookingStatus.CANCELLED
                        && SearchService.overlaps(from, until, b.getCheckIn(), b.getCheckOut()));
        boolean maintenanceConflict = store.getMaintenanceBlocksForRoom(roomId).stream()
                .anyMatch(b -> SearchService.overlaps(from, until, b.getCheckIn(), b.getCheckOut()));
        if (bookingConflict || maintenanceConflict) {
            throw new BusinessRuleException("Room has an overlapping booking or maintenance block.");
        }
        MaintenanceBlock block = new MaintenanceBlock(Ids.newId("MNT"), room.getId(), from, until);
        store.addMaintenanceBlock(block);
        return block;
    }
    public List<MaintenanceBlock> listBlocks() { return store.getMaintenanceBlocks(); }
}
