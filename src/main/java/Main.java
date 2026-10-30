package com.hotelbooking;

import com.hotelbooking.model.*;
import com.hotelbooking.payment.*;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.service.*;
import com.hotelbooking.ui.ConsoleUI;

import java.math.BigDecimal;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        InMemoryDataStore store = new InMemoryDataStore();
        HotelService hotelService = new HotelService(store);
        RoomService roomService = new RoomService(store);
        SearchService searchService = new SearchService(store);
        PricingService pricingService = new PricingService(store);
        PaymentService paymentService = new PaymentService(store);
        BookingService bookingService = new BookingService(
                store, searchService, pricingService, paymentService);
        ReviewService reviewService = new ReviewService(store);
        MaintenanceService maintenanceService = new MaintenanceService(store);
        AuthenticationService authenticationService = new AuthenticationService(store);

        seedData(authenticationService, hotelService, roomService);
        new ConsoleUI(authenticationService, hotelService, roomService, searchService, bookingService,
                paymentService, pricingService, reviewService, maintenanceService, store).run();
    }

    private static void seedData(AuthenticationService auth, HotelService hotels, RoomService rooms) {
        auth.createAdmin("Admin", "admin@hotel.example", "9000000001", "Admin123!");
        HotelManager manager = auth.createHotelManager(
                "Asha Manager", "asha@hotel.example", "9000000002", "Manager123!");
        auth.registerGuest(
                "Demo Guest", "guest@hotel.example", "9000000003",
                IdProofType.AADHAAR, "123456789012", "Guest123!");

        Hotel chennai = hotels.createHotel("Coastal Comfort", "Chennai",
                "12 Marina Road, Chennai", manager.getId());
        Hotel bengaluru = hotels.createHotel("Garden Stay", "Bengaluru",
                "8 Residency Road, Bengaluru", manager.getId());

        rooms.createRoom(chennai.getId(), "C-101", RoomType.SINGLE, 1, new BigDecimal("2200.00"));
        rooms.createRoom(chennai.getId(), "C-202", RoomType.DOUBLE, 2, new BigDecimal("3600.00"));
        rooms.createRoom(chennai.getId(), "C-501", RoomType.SUITE, 4, new BigDecimal("8500.00"));
        rooms.createRoom(bengaluru.getId(), "B-110", RoomType.DELUXE, 2, new BigDecimal("5200.00"));
        rooms.createRoom(bengaluru.getId(), "B-210", RoomType.DOUBLE, 2, new BigDecimal("3900.00"));
    }
}
