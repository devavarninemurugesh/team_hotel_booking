# Hotel Booking System

A Java 21 console application demonstrating object-oriented design and low-level
design practices. Runtime records are held in one shared `InMemoryDataStore`;
there is no database connection or persistence between runs. SQL files are
schema/query design exercises only.

## Requirements and running

- JDK 21+
- Apache Maven 3.8+

Run the application with `mvn compile exec:java -Dexec.mainClass=com.hotelbooking.Main`.
The project uses the `maven.compiler.release` setting of 21.

Run tests using `mvn test`.

The demo seed accounts are:

| Role | Email | Demo password |
|---|---|---|
| Admin | `admin@hotel.example` | `Admin123!` |
| Hotel manager | `asha@hotel.example` | `Manager123!` |
| Guest | `guest@hotel.example` | `Guest123!` |

These are demonstration credentials, not production credentials. Passwords are
salted and hashed in memory using PBKDF2-HMAC-SHA-256; the sample passwords are
public and data disappears at process exit. Do not use this authentication or
payment simulation in a real service.

## Packages and ownership

- `com.hotelbooking.Main` owns the sole application entry point, dependency
  construction, and sample-data seeding.
- `model` owns domain entities, role/status enums, and their invariants.
- `repository.InMemoryDataStore` is the only owner of shared runtime collections.
  Services receive this same instance through constructor injection; collection
  accessors return snapshots and add methods reject duplicate IDs.
- `service` owns authentication, hotel/room/maintenance, availability, pricing,
  booking, payment orchestration, and review rules.
- `payment` defines the method abstraction and demo UPI/card/pay-at-hotel options.
- `ui.ConsoleUI` owns terminal prompts and role-specific menus, not business rules.
- `exception` and `util` contain shared failure types, ID generation, and password hashing.

The manager/admin menus are role-gated after authentication. Manager operations
also verify that the selected hotel belongs to that manager.

## Main service contracts

| Service | Signature | Behavior |
|---|---|---|
| SearchService | `List<Room> searchAvailableRooms(String city, LocalDate checkIn, LocalDate checkOut, int guestCount)` | Filters rooms by city, capacity, date overlap, bookings, and maintenance |
| SearchService | `boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut)` | Checks half-open date interval conflicts |
| PricingService | `BigDecimal calculateBookingPrice(List<String> roomIds, LocalDate checkIn, LocalDate checkOut)` | Adds room nightly rates times nights |
| BookingService | `Booking createBooking(String guestId, String hotelId, List<String> roomIds, LocalDate checkIn, LocalDate checkOut, int guestCount)` | Validates ownership, capacity, dates, availability, and records the booking |
| BookingService | `BigDecimal cancelBooking(String bookingId, boolean initiatedByHotel)` | Cancels eligible booking and applies the refund policy to captured payments |
| BookingService | `void checkIn(String bookingId, boolean idVerified)` | Requires a verified ID and an active booking date |
| BookingService | `void checkOut(String bookingId)` | Completes a checked-in stay and settles pay-at-hotel payment |
| PaymentService | `Payment processPayment(String bookingId, PaymentMethod method, BigDecimal amount)` | Requires exact booking total; confirms booking after authorization |
| PaymentService | `Payment refundPayment(String bookingId, BigDecimal amount)` | Refunds captured payment within the original captured amount |
| ReviewService | `Review addReview(String bookingId, String guestId, int rating, String comment)` | Allows one review by the booking guest after checkout |

Availability uses `[checkIn, checkOut)` intervals: a room can be booked again on
the previous guest's checkout date. The guest cancellation policy returns the
full captured amount at least 48 hours before check-in and 50% closer to check-in;
hotel-initiated cancellation is eligible for a full refund. No refund is issued
for pay-at-hotel reservations before a payment is captured.

UPI/card authorization is a local demonstration simulation, not a payment
gateway. The card flow asks for a demo token only; never enter a card number.

## Team ownership baseline

| Owner | Files / responsibility |
|---|---|
| Team leader | `Main.java`, shared user hierarchy, enums, `InMemoryDataStore`, shared utilities/exceptions, integration and docs |
| Member 2 | Hotel and room models/services; maintenance model/service |
| Member 3 | Availability and search |
| Member 4 | Booking models and booking lifecycle |
| Member 5 | Payment models/adapters, pricing, reviews/refunds, SQL exercises |

`Main.java` and the application package structure are shared integration contracts;
contributing branches should not add another entry point or independent store.

## Project map

```text
src/main/java/Main.java
src/main/java/com/hotelbooking/{model,service,repository,payment,exception,ui,util}/
src/test/java/com/hotelbooking/
docs/{er_diagram,system_architecture,class_diagram}.md
schema.sql
hotel_booking_queries.sql
```
