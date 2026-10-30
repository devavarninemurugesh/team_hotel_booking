# Entity relationship diagram

The diagram models the SQL exercise schema in [schema.sql](../schema.sql). It
represents the same domain relationships as the runtime model; the application
does not connect to a relational database.

```mermaid
erDiagram
    USERS ||--o{ HOTELS : manages
    USERS ||--o{ BOOKINGS : makes
    HOTELS ||--o{ ROOMS : contains
    HOTELS ||--o{ BOOKINGS : receives
    BOOKINGS ||--|{ BOOKING_ROOMS : reserves
    ROOMS ||--o{ BOOKING_ROOMS : assigned
    BOOKINGS ||--o{ PAYMENTS : paid_by
    ROOMS ||--o{ MAINTENANCE_BLOCKS : blocked_for
    BOOKINGS ||--o| REVIEWS : reviewed_by
    USERS ||--o{ REVIEWS : writes
    HOTELS ||--o{ REVIEWS : receives

    USERS {
        string user_id PK
        string name
        string email UK
        string phone
        string role
        string password_hash
        string id_proof_type
        string id_proof_number
    }
    HOTELS {
        string hotel_id PK
        string manager_id FK
        string name
        string city
        string address
    }
    ROOMS {
        string room_id PK
        string hotel_id FK
        string room_number
        string room_type
        int capacity
        decimal nightly_rate
    }
    BOOKINGS {
        string booking_id PK
        string guest_id FK
        string hotel_id FK
        date check_in
        date check_out
        decimal total_price
        string status
    }
    BOOKING_ROOMS {
        string booking_id PK, FK
        string room_id PK, FK
    }
    PAYMENTS {
        string payment_id PK
        string booking_id FK
        string method
        decimal amount
        string status
    }
    MAINTENANCE_BLOCKS {
        string maintenance_id PK
        string room_id FK
        date check_in
        date check_out
    }
    REVIEWS {
        string review_id PK
        string booking_id FK, UK
        string guest_id FK
        string hotel_id FK
        int rating
        string comment
    }
```
