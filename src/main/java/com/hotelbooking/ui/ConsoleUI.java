package com.hotelbooking.ui;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.model.*;
import com.hotelbooking.payment.*;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class ConsoleUI {
    private final Scanner input = new Scanner(System.in);
    private final AuthenticationService authentication;
    private final HotelService hotels;
    private final RoomService rooms;
    private final SearchService search;
    private final BookingService bookings;
    private final PaymentService payments;
    private final PricingService pricing;
    private final ReviewService reviews;
    private final MaintenanceService maintenance;
    private final InMemoryDataStore store;

    public ConsoleUI(AuthenticationService authentication, HotelService hotels, RoomService rooms,
                     SearchService search, BookingService bookings, PaymentService payments,
                     PricingService pricing, ReviewService reviews, MaintenanceService maintenance,
                     InMemoryDataStore store) {
        this.authentication = authentication;
        this.hotels = hotels;
        this.rooms = rooms;
        this.search = search;
        this.bookings = bookings;
        this.payments = payments;
        this.pricing = pricing;
        this.reviews = reviews;
        this.maintenance = maintenance;
        this.store = store;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Hotel Booking System ===");
            System.out.println("1. Guest login  2. Guest registration  3. Hotel manager login");
            System.out.println("4. Admin login  0. Exit");
            String choice = prompt("Choose: ");
            try {
                switch (choice) {
                    case "1" -> login(UserRole.GUEST);
                    case "2" -> registerGuest();
                    case "3" -> login(UserRole.HOTEL_MANAGER);
                    case "4" -> login(UserRole.ADMIN);
                    case "0" -> running = false;
                    default -> System.out.println("Choose one of the displayed options.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Unable to complete that action: " + ex.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    private void login(UserRole expectedRole) {
        String email = prompt("Email: ");
        String password = prompt("Password: ");
        User user = authentication.authenticate(email, password)
                .filter(candidate -> candidate.getRole() == expectedRole)
                .orElseThrow(() -> new BusinessRuleException("Invalid credentials for the selected role."));
        System.out.println("Welcome, " + user.getName() + ".");
        switch (user.getRole()) {
            case GUEST -> guestMenu((Guest) user);
            case HOTEL_MANAGER -> managerMenu((HotelManager) user);
            case ADMIN -> adminMenu();
        }
    }

    private void registerGuest() {
        String name = prompt("Name: ");
        String email = prompt("Email: ");
        String phone = prompt("Phone (10-15 digits): ");
        IdProofType proofType = enumChoice(IdProofType.values(), "ID proof type");
        String proofNumber = prompt("ID proof number: ");
        String password = prompt("Password (at least 8 characters): ");
        Guest guest = authentication.registerGuest(name, email, phone, proofType, proofNumber, password);
        System.out.println("Registration complete. Your guest ID is " + guest.getId() + ". Please log in.");
    }

    private void guestMenu(Guest guest) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nGuest menu: 1.Search/book 2.My bookings 3.Cancel booking");
            System.out.println("4.Check-in 5.Check-out 6.Review 7.Hotels/reviews 0.Logout");
            try {
                switch (prompt("Choose: ")) {
                    case "1" -> searchAndBook(guest);
                    case "2" -> showBookings(bookings.getBookingsForGuest(guest.getId()));
                    case "3" -> cancelBooking(guest);
                    case "4" -> checkIn(guest);
                    case "5" -> checkOut(guest);
                    case "6" -> addReview(guest);
                    case "7" -> showHotelsAndReviews();
                    case "0" -> loggedIn = false;
                    default -> System.out.println("Choose one of the displayed options.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Unable to complete that action: " + ex.getMessage());
            }
        }
    }

    private void searchAndBook(Guest guest) {
        String city = prompt("City: ");
        LocalDate checkIn = LocalDate.parse(prompt("Check-in (YYYY-MM-DD): "));
        LocalDate checkOut = LocalDate.parse(prompt("Check-out (YYYY-MM-DD): "));
        int guestCount = Integer.parseInt(prompt("Number of guests: "));
        List<Room> available = search.searchAvailableRooms(city, checkIn, checkOut, guestCount);
        if (available.isEmpty()) {
            System.out.println("No available rooms match those dates and guest count.");
            return;
        }
        available.forEach(room -> printRoom(room));
        String hotelId = prompt("Hotel ID to book: ");
        String idsInput = prompt("Room ID(s), comma separated: ");
        List<String> roomIds = Arrays.stream(idsInput.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        BigDecimal total = pricing.calculateBookingPrice(roomIds, checkIn, checkOut);
        System.out.println("Total: INR " + total);
        Booking booking = bookings.createBooking(guest.getId(), hotelId, roomIds, checkIn, checkOut, guestCount);
        try {
            PaymentMethod method = choosePaymentMethod();
            Payment payment = payments.processPayment(booking.getId(), method, total);
            System.out.println("Booking " + booking.getId() + " is " + booking.getStatus()
                    + ". Payment status: " + payment.getStatus() + ".");
        } catch (RuntimeException ex) {
            bookings.cancelBooking(booking.getId(), true);
            throw ex;
        }
    }

    private PaymentMethod choosePaymentMethod() {
        System.out.println("Payment: 1.UPI 2.Card token (demo) 3.Pay at hotel");
        return switch (prompt("Choose: ")) {
            case "1" -> new UpiPayment(prompt("UPI ID: "));
            case "2" -> new CardPayment(prompt("Demo payment token (do not enter card details): "));
            case "3" -> new PayAtHotelPayment();
            default -> throw new BusinessRuleException("Unknown payment option.");
        };
    }

    private void cancelBooking(Guest guest) {
        String id = prompt("Booking ID: ");
        if (bookings.getBookingsForGuest(guest.getId()).stream().noneMatch(b -> b.getId().equals(id))) {
            throw new BusinessRuleException("That booking does not belong to your account.");
        }
        BigDecimal refund = bookings.cancelBooking(id, false);
        System.out.println("Booking cancelled. Refund eligible: INR " + refund);
    }

    private void checkIn(Guest guest) {
        Booking booking = ownedBooking(guest);
        boolean verified = prompt("Has the guest's government ID been checked? (y/N): ").equalsIgnoreCase("y");
        bookings.checkIn(booking.getId(), verified);
        System.out.println("Check-in complete.");
    }

    private void checkOut(Guest guest) {
        Booking booking = ownedBooking(guest);
        bookings.checkOut(booking.getId());
        System.out.println("Checkout complete.");
    }

    private void addReview(Guest guest) {
        Booking booking = ownedBooking(guest);
        int rating = Integer.parseInt(prompt("Rating (1-5): "));
        String comment = prompt("Comment: ");
        Review review = reviews.addReview(booking.getId(), guest.getId(), rating, comment);
        System.out.println("Review saved for hotel " + review.getHotelId() + ".");
    }

    private Booking ownedBooking(Guest guest) {
        String id = prompt("Booking ID: ");
        return bookings.getBookingsForGuest(guest.getId()).stream()
                .filter(b -> b.getId().equals(id)).findFirst()
                .orElseThrow(() -> new BusinessRuleException("That booking does not belong to your account."));
    }

    private void showBookings(List<Booking> records) {
        if (records.isEmpty()) System.out.println("No bookings.");
        records.forEach(booking -> {
            String roomNumbers = bookings.getRoomsForBooking(booking.getId()).stream()
                    .map(br -> store.findRoom(br.roomId()).map(Room::getRoomNumber).orElse("unknown"))
                    .reduce((a, b) -> a + ", " + b).orElse("none");
            System.out.printf("%s | hotel %s | rooms %s | %s to %s | INR %s | %s%n",
                    booking.getId(), booking.getHotelId(), roomNumbers, booking.getCheckIn(),
                    booking.getCheckOut(), booking.getTotalPrice(), booking.getStatus());
        });
    }

    private void showHotelsAndReviews() {
        hotels.listHotels().forEach(hotel -> {
            System.out.printf("%s | %s | %s | %s%n", hotel.getId(), hotel.getName(), hotel.getCity(), hotel.getAddress());
            reviews.getReviewsForHotel(hotel.getId()).forEach(review ->
                    System.out.printf("  %d/5: %s%n", review.getRating(), review.getComment()));
        });
    }

    private void managerMenu(HotelManager manager) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nManager menu: 1.My hotels/rooms 2.Add room 3.Block room for maintenance 0.Logout");
            try {
                switch (prompt("Choose: ")) {
                    case "1" -> showManagedHotels(manager);
                    case "2" -> addRoom(manager);
                    case "3" -> addMaintenance(manager);
                    case "0" -> loggedIn = false;
                    default -> System.out.println("Choose one of the displayed options.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Unable to complete that action: " + ex.getMessage());
            }
        }
    }

    private void showManagedHotels(HotelManager manager) {
        List<Hotel> managed = hotels.hotelsManagedBy(manager.getId());
        if (managed.isEmpty()) System.out.println("No hotels are assigned to this manager.");
        managed.forEach(hotel -> {
            System.out.printf("%s | %s | %s%n", hotel.getId(), hotel.getName(), hotel.getCity());
            rooms.getRoomsForHotel(hotel.getId()).forEach(this::printRoom);
        });
    }

    private void addRoom(HotelManager manager) {
        Hotel hotel = chooseManagedHotel(manager);
        String number = prompt("Room number: ");
        RoomType type = enumChoice(RoomType.values(), "room type");
        int capacity = Integer.parseInt(prompt("Guest capacity: "));
        BigDecimal rate = new BigDecimal(prompt("Nightly rate in INR: "));
        printRoom(rooms.createRoom(hotel.getId(), number, type, capacity, rate));
    }

    private void addMaintenance(HotelManager manager) {
        showManagedHotels(manager);
        String roomId = prompt("Room ID: ");
        Room room = store.findRoom(roomId).orElseThrow(() -> new BusinessRuleException("Room was not found."));
        if (hotels.hotelsManagedBy(manager.getId()).stream().noneMatch(h -> h.getId().equals(room.getHotelId()))) {
            throw new BusinessRuleException("This room is not in a hotel assigned to your account.");
        }
        LocalDate from = LocalDate.parse(prompt("Maintenance from (YYYY-MM-DD): "));
        LocalDate until = LocalDate.parse(prompt("Maintenance until (YYYY-MM-DD): "));
        maintenance.blockRoom(roomId, from, until);
        System.out.println("Maintenance period saved.");
    }

    private Hotel chooseManagedHotel(HotelManager manager) {
        List<Hotel> managed = hotels.hotelsManagedBy(manager.getId());
        if (managed.isEmpty()) throw new BusinessRuleException("No hotels are assigned to your account.");
        managed.forEach(h -> System.out.printf("%s | %s | %s%n", h.getId(), h.getName(), h.getCity()));
        String id = prompt("Hotel ID: ");
        return managed.stream().filter(h -> h.getId().equals(id)).findFirst()
                .orElseThrow(() -> new BusinessRuleException("Hotel is not assigned to this manager."));
    }

    private void adminMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nAdmin menu: 1.Hotels and rooms 2.Users 3.All bookings 0.Logout");
            try {
                switch (prompt("Choose: ")) {
                    case "1" -> {
                        hotels.listHotels().forEach(h -> {
                            System.out.printf("%s | %s | %s%n", h.getId(), h.getName(), h.getCity());
                            rooms.getRoomsForHotel(h.getId()).forEach(this::printRoom);
                        });
                    }
                    case "2" -> store.getUsers().forEach(u ->
                            System.out.printf("%s | %s | %s | %s%n", u.getId(), u.getName(), u.getEmail(), u.getRole()));
                    case "3" -> showBookings(store.getBookings());
                    case "0" -> loggedIn = false;
                    default -> System.out.println("Choose one of the displayed options.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Unable to complete that action: " + ex.getMessage());
            }
        }
    }

    private void printRoom(Room room) {
        String hotel = store.findHotel(room.getHotelId()).map(Hotel::getName).orElse("unknown hotel");
        System.out.printf("Room %s | hotel %s (%s) | %s | %s | capacity %d | INR %s/night%n",
                room.getId(), room.getHotelId(), hotel, room.getRoomNumber(), room.getType(),
                room.getCapacity(), room.getNightlyRate());
    }

    private String prompt(String label) {
        System.out.print(label);
        return input.nextLine().trim();
    }

    private <T extends Enum<T>> T enumChoice(T[] values, String label) {
        System.out.println(label + ": " + Arrays.toString(values));
        String choice = prompt("Choose: ");
        return Arrays.stream(values).filter(value -> value.name().equalsIgnoreCase(choice))
                .findFirst().orElseThrow(() -> new BusinessRuleException("Invalid " + label + "."));
    }
}
