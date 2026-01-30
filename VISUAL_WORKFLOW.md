# CRCS — End-to-End Visual Workflow

This document describes the main flows of the Campus Resource Coordination System using diagrams and short explanations. All diagrams use [Mermaid](https://mermaid.js.org/) and render in GitHub, GitLab, and most Markdown viewers.

---

## 1. System Overview

```mermaid
flowchart LR
    subgraph Client
        FE[Frontend<br/>React + Vite]
    end

    subgraph Gateway
        GW[API Gateway<br/>:6000]
    end

    subgraph Services
        AUTH[Auth Service<br/>:6001]
        USER[User Service<br/>:6002]
        RES[Resource Service<br/>:6003]
        BOOK[Booking Service<br/>:6004]
        NOTIF[Notification Service<br/>:6005]
    end

    subgraph Data
        MYSQL[(MySQL)]
        KAFKA[Kafka<br/>crcs-notification]
    end

    FE --> GW
    GW --> AUTH
    GW --> USER
    GW --> RES
    GW --> BOOK
    AUTH --> MYSQL
    USER --> MYSQL
    RES --> MYSQL
    BOOK --> MYSQL
    AUTH -.->|publish| KAFKA
    BOOK -.->|publish| KAFKA
    KAFKA --> NOTIF
    NOTIF -->|SMTP| EMAIL[Email]
```

- **Frontend** talks only to the **API Gateway** (single entry point).
- **Gateway** validates JWT (except for `/auth/**`) and routes to the correct service.
- **Auth** and **Booking** publish events to **Kafka**; **Notification Service** consumes them and sends emails.

---

## 2. API Gateway Request Flow

```mermaid
flowchart TD
    REQ[Request from Frontend] --> GW[API Gateway :6000]
    GW --> PATH{Path?}
    PATH -->|/auth/**| AUTH[Auth Service :6001]
    PATH -->|/users/**| JWT1[JWT Validation] --> USER[User Service :6002]
    PATH -->|/resources/**| JWT2[JWT Validation] --> RES[Resource Service :6003]
    PATH -->|/resources/manage/**| JWT3[JWT + Role Check] --> RES
    PATH -->|/bookings/**| JWT4[JWT Validation] --> BOOK[Booking Service :6004]
    AUTH --> R1[Response]
    USER --> R1
    RES --> R1
    BOOK --> R1
```

- **`/auth/**`** — No JWT; used for login, signup, refresh, validate.
- **`/users/**`, `/resources/**`, `/bookings/**`** — Require valid JWT.
- **`/resources/manage/**`** — Requires JWT + RESOURCE_MANAGER (or higher) role.

---

## 3. Signup (Registration) Flow

```mermaid
sequenceDiagram
    participant U as User / Frontend
    participant GW as API Gateway
    participant AUTH as Auth Service
    participant A_DB as Auth DB (users)
    participant USVC as User Service
    participant U_DB as User DB (user_profile)
    participant K as Kafka
    participant NS as Notification Service
    participant SMTP as Email (SMTP)

    U->>GW: POST /auth/signup (email, password, name, role)
    GW->>AUTH: Forward
    AUTH->>A_DB: Check email exists
    alt Email already exists
        AUTH-->>U: 4xx Email taken
    else Email free
        AUTH->>A_DB: Save user (id, email, hashed password, role)
        AUTH->>USVC: Create user profile (userId, email, name, role)
        USVC->>U_DB: Insert user_profile
        AUTH->>AUTH: Generate JWT + refresh token
        AUTH->>A_DB: Save refresh token
        AUTH->>K: Publish WELCOME event
        K->>NS: Consume CREATE_NOTIFICATION
        NS->>SMTP: Send welcome email
        AUTH-->>U: 200 + tokens + userInfo
    end
```

- User is stored in **Auth DB** and a profile is created in **User Service**.
- **Welcome email** is sent asynchronously via Kafka → Notification Service → SMTP.

---

## 4. Login Flow

```mermaid
sequenceDiagram
    participant U as User / Frontend
    participant GW as API Gateway
    participant AUTH as Auth Service
    participant A_DB as Auth DB

    U->>GW: POST /auth/login (email, password)
    GW->>AUTH: Forward
    AUTH->>A_DB: Find user by email
    alt Invalid credentials
        AUTH-->>U: 401 Unauthorized
    else Valid
        AUTH->>AUTH: Verify password, generate JWT + refresh token
        AUTH->>A_DB: Save refresh token
        AUTH-->>U: 200 + accessToken, refreshToken, userInfo (userId, email, role)
    end
```

- Frontend stores **access token** and **refresh token**; access token is sent in `Authorization` for protected APIs.

---

## 5. Create Booking Flow (End-to-End)

```mermaid
sequenceDiagram
    participant U as User / Frontend
    participant GW as API Gateway
    participant BOOK as Booking Service
    participant RES as Resource Service
    participant USVC as User Service
    participant B_DB as Booking DB
    participant K as Kafka
    participant NS as Notification Service
    participant SMTP as Email

    U->>GW: POST /bookings (resourceId, start, end, purpose) [JWT]
    GW->>GW: Validate JWT, extract userId
    GW->>BOOK: Forward + userId from token

    BOOK->>RES: Get resource by id
    RES-->>BOOK: Resource (status, name)
    alt Resource not found / not AVAILABLE
        BOOK-->>U: 4xx / unavailable
    end

    BOOK->>BOOK: Check conflicting bookings in DB
    alt Conflict
        BOOK-->>U: 4xx Already booked
    end

    BOOK->>B_DB: Save booking (status = PENDING)
    BOOK->>USVC: Get user by userId (email, name)
    USVC-->>BOOK: User profile
    BOOK->>K: Publish BOOKING_REQUEST_SUBMITTED
    K->>NS: Consume CREATE_NOTIFICATION
    NS->>SMTP: Send "booking request submitted" email
    BOOK-->>U: 200 + booking (PENDING)
```

- Booking is created in **PENDING** state. Resource status is not changed until approval.
- User receives an email that their **booking request** was submitted.

---

## 6. Approve Booking Flow (Facility Manager)

```mermaid
sequenceDiagram
    participant FM as Facility Manager / Frontend
    participant GW as API Gateway
    participant BOOK as Booking Service
    participant USVC as User Service
    participant K as Kafka
    participant NS as Notification Service
    participant SMTP as Email

    FM->>GW: POST /bookings/{id}/approve [JWT, role=FACILITY_MANAGER]
    GW->>GW: Validate JWT + role
    GW->>BOOK: Approve booking

    BOOK->>BOOK: Load booking, ensure status = PENDING
    BOOK->>BOOK: Set status = CONFIRMED, save
    BOOK->>USVC: Get user by userId
    USVC-->>BOOK: User (email, name)
    BOOK->>K: Publish BOOKING_CONFIRMED
    K->>NS: Consume CREATE_NOTIFICATION
    NS->>SMTP: Send "booking confirmed" email
    BOOK-->>FM: 200 + confirmed booking
```

- Only **FACILITY_MANAGER** (or ADMIN) can approve; this is enforced at the gateway/service layer.
- Booker receives a **booking confirmed** email.

---

## 7. Cancel Booking Flow

```mermaid
sequenceDiagram
    participant U as User / Frontend
    participant GW as API Gateway
    participant BOOK as Booking Service
    participant USVC as User Service
    participant K as Kafka
    participant NS as Notification Service
    participant SMTP as Email

    U->>GW: POST /bookings/{id}/cancel [JWT]
    GW->>BOOK: Cancel booking (userId from JWT)

    BOOK->>BOOK: Find booking, verify userId owns it
    BOOK->>BOOK: Set status = CANCELLED, save
    BOOK->>USVC: Get user by userId
    USVC-->>BOOK: User (email, name)
    BOOK->>K: Publish BOOKING_CANCELLED
    K->>NS: Consume CREATE_NOTIFICATION
    NS->>SMTP: Send "booking cancelled" email
    BOOK-->>U: 200
```

- Only the **booking owner** can cancel. Notification service sends a **booking cancelled** email.

---

## 8. Notification Pipeline (Kafka → Email)

```mermaid
flowchart LR
    subgraph Producers
        AUTH[Auth Service<br/>Signup]
        BOOK[Booking Service<br/>Create/Approve/Cancel]
    end

    subgraph Events
        E1[WELCOME]
        E2[BOOKING_REQUEST_SUBMITTED]
        E3[BOOKING_CONFIRMED]
        E4[BOOKING_CANCELLED]
    end

    subgraph Kafka
        TOPIC[(crcs-notification)]
    end

    subgraph Consumer
        NS[Notification Service]
    end

    subgraph Templates
        T1[EmailTemplateWelcome]
        T2[EmailTemplateBookingRequestSubmitted]
        T3[EmailTemplateBookingConfirmed]
        T4[EmailTemplateBookingCancelled]
    end

    AUTH --> E1
    BOOK --> E2
    BOOK --> E3
    BOOK --> E4
    E1 --> TOPIC
    E2 --> TOPIC
    E3 --> TOPIC
    E4 --> TOPIC
    TOPIC --> NS
    NS --> T1
    NS --> T2
    NS --> T3
    NS --> T4
    T1 --> SMTP[SMTP]
    T2 --> SMTP
    T3 --> SMTP
    T4 --> SMTP
```

- All notification events are **CREATE_NOTIFICATION** with a template type and payload.
- **Notification Service** consumes from `crcs-notification`, picks the right email template, and sends via SMTP.

---

## 9. Booking State Lifecycle

```mermaid
stateDiagram-v2
    [*] --> PENDING: User creates booking
    PENDING --> CONFIRMED: Facility Manager approves
    PENDING --> CANCELLED: User or FM cancels
    CONFIRMED --> CANCELLED: User cancels
```

- **PENDING**: Just created; waiting for approval.
- **CONFIRMED**: Approved by Facility Manager.
- **CANCELLED**: From either PENDING or CONFIRMED; cancellation email is sent when applicable.

---

## 10. Role-Based Access (Summary)

```mermaid
flowchart TD
    subgraph USER[USER]
        V_RES[View resources]
        CREATE_B[Create booking]
        MY_B[My bookings]
        CANCEL_B[Cancel own booking]
    end

    subgraph RM[RESOURCE_MANAGER]
        CRUD_RES[CRUD resources]
        ALL_B[View all bookings]
    end

    subgraph FM[FACILITY_MANAGER]
        APPROVE[Approve / Reject bookings]
    end

    subgraph ADMIN[ADMIN]
        USERS[Manage users]
        CONFIG[System config]
    end

    USER --> RM
    RM --> FM
    FM --> ADMIN
```

- **USER**: View resources, create/manage own bookings.
- **RESOURCE_MANAGER**: Plus resource CRUD and view all bookings.
- **FACILITY_MANAGER**: Plus approve/reject pending bookings.
- **ADMIN**: Full access including user management.

---

## Summary

| Flow              | Entry point        | Key services              | Async (Kafka)     |
|-------------------|--------------------|---------------------------|--------------------|
| Signup            | POST /auth/signup  | Auth, User, Notification  | Welcome email      |
| Login             | POST /auth/login   | Auth                      | —                  |
| Create booking    | POST /bookings     | Booking, Resource, User   | Request submitted  |
| Approve booking   | POST /bookings/…/approve | Booking, User      | Booking confirmed  |
| Cancel booking    | POST /bookings/…/cancel  | Booking, User      | Booking cancelled  |

All user-facing traffic goes through the **API Gateway**; **Auth** and **Booking** drive the **notification** flow via **Kafka** to the **Notification Service**, which sends emails using the configured SMTP settings.
