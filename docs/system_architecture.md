# System architecture

```mermaid
flowchart TD
    Main["Main (only entry point)<br/>wires dependencies and seeds demo data"]
    UI["ConsoleUI<br/>role menus and terminal I/O"]
    Auth["AuthenticationService<br/>PBKDF2 password verification"]
    HotelSvc["HotelService / RoomService"]
    Search["SearchService"]
    Booking["BookingService"]
    Pricing["PricingService"]
    Payment["PaymentService"]
    Maintenance["MaintenanceService"]
    Review["ReviewService"]
    Store[("InMemoryDataStore<br/>single shared collection owner")]
    Models["Domain models and enums"]
    Methods["PaymentMethod implementations<br/>UPI / Card / Pay at hotel"]

    Main --> UI
    Main --> Store
    Main --> Auth
    Main --> HotelSvc
    Main --> Search
    Main --> Booking
    Main --> Pricing
    Main --> Payment
    Main --> Maintenance
    Main --> Review
    UI --> Auth
    UI --> HotelSvc
    UI --> Search
    UI --> Booking
    UI --> Payment
    UI --> Pricing
    UI --> Maintenance
    UI --> Review
    Booking --> Search
    Booking --> Pricing
    Booking --> Payment
    Payment --> Methods
    Auth --> Store
    HotelSvc --> Store
    Search --> Store
    Booking --> Store
    Pricing --> Store
    Payment --> Store
    Maintenance --> Store
    Review --> Store
    Store --> Models
```

Services are constructed once and receive the same store instance. The UI never
owns persistence and contains no pricing, availability, refund, or lifecycle rules.
Console role routing limits visible actions; manager-specific hotel choices are
verified against ownership before mutation.
