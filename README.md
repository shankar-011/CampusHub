# CampusHub Backend

A Spring Boot REST API for campus event management. Handles user authentication, event CRUD, atomic ticket booking, and role-based access control.

## Tech Stack

- **Java 17** + **Spring Boot 3.3**
- **PostgreSQL** (Neon) — persistent storage
- **Redis** (Redis Cloud) — refresh tokens + OTP storage
- **JJWT** — JWT access/refresh tokens
- **Spring Mail** — OTP email delivery via Gmail SMTP
- **SpringDoc OpenAPI** — Swagger UI

## Features

- Email OTP verification before registration
- JWT auth — 15 min access tokens, 7 day refresh tokens stored in Redis
- Role-based access: `STUDENT` (default) · `ORGANIZER` · `ADMIN`
- Atomic ticket booking — single SQL UPDATE prevents overbooking under concurrent load
- Admin panel — manage users, events, and bookings

## Getting Started

### Prerequisites

- Java 17+
- Maven
- A [Neon](https://neon.tech) PostgreSQL database
- A [Redis Cloud](https://app.redislabs.com) instance
- A Gmail account with an [App Password](https://myaccount.google.com/apppasswords)

### Setup

1. Clone the repo and navigate to the project:
   ```bash
   git clone <repo-url>
   cd campushub-backend
   ```

2. Create a `.env` file in `campushub-backend/`:
   ```env
   DATABASE_URL=jdbc:postgresql://<neon-host>/neondb?sslmode=require
   DATABASE_USERNAME=neondb_owner
   DATABASE_PASSWORD=your-neon-password
   REDIS_URL=redis://default:password@host:port
   JWT_SECRET=your-secret-key-at-least-32-chars
   MAIL_USERNAME=your-gmail@gmail.com
   MAIL_PASSWORD=your-gmail-app-password
   ```

3. Run the schema against your Neon database (first time only):
   ```bash
   # The app runs schema.sql automatically on startup via spring.sql.init.mode=always
   ```

4. Start the app:
   ```bash
   mvn spring-boot:run
   ```

5. Open Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## API Overview

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/send-otp` | Send OTP to email (step 1 of registration) |
| `POST` | `/api/auth/register` | Register with OTP (step 2) |
| `POST` | `/api/auth/login` | Login — returns access + refresh tokens |
| `POST` | `/api/auth/refresh` | Exchange refresh token for new access token |

### Events

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/events` | List all events |
| `GET` | `/api/events/{id}` | Get event by ID |
| `POST` | `/api/events` | Create event (ORGANIZER/ADMIN) |
| `PUT` | `/api/events/{id}` | Update event (owner/ADMIN) |
| `DELETE` | `/api/events/{id}` | Delete event (owner/ADMIN) |

### Bookings

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/bookings` | Book tickets for an event |
| `GET` | `/api/bookings` | Get my bookings |
| `GET` | `/api/bookings/{id}` | Get booking by ID |

### Admin (ADMIN role required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/users` | List all users |
| `GET` | `/api/admin/users/search?email=` | Find user by email |
| `PATCH` | `/api/admin/users/promote-organizer?email=` | Promote to ORGANIZER |
| `PATCH` | `/api/admin/users/demote-student?email=` | Demote to STUDENT |
| `DELETE` | `/api/admin/users?email=` | Delete user |
| `GET` | `/api/admin/events` | List all events |
| `DELETE` | `/api/admin/events/{id}` | Delete any event |
| `GET` | `/api/admin/bookings` | List all bookings |
| `GET` | `/api/admin/bookings/event/{id}` | Bookings for an event |
| `GET` | `/api/admin/bookings/user?email=` | Bookings for a user |

## Authentication

All protected endpoints require a `Bearer` token in the `Authorization` header:

```
Authorization: Bearer <accessToken>
```

Use `POST /api/auth/login` to get a token, then click **Authorize** in Swagger UI.

## Role Management

All users self-register as `STUDENT`. To elevate roles:

- **Make someone an admin** — update directly in the DB:
  ```sql
  UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
  ```
- **Make someone an organizer** — use the admin endpoint:
  ```
  PATCH /api/admin/users/promote-organizer?email=organizer@example.com
  ```

## Registration Flow

```
POST /api/auth/send-otp   →   { "email": "user@example.com" }
                                        ↓
                              OTP sent to email (valid 10 min)
                                        ↓
POST /api/auth/register   →   { "name": "...", "email": "...", "password": "...", "otp": "123456" }
```

## Project Structure

```
src/main/java/com/campushub/
├── config/        # Security, Redis, JWT filter, OpenAPI
├── controller/    # Auth, Event, Booking, Admin controllers
├── dto/           # Request/response records
├── entity/        # JPA entities (User, Event, Booking)
├── exception/     # Custom exceptions + GlobalExceptionHandler
├── repository/    # Spring Data JPA repositories
├── service/       # AuthService, EventService, BookingService, AdminService, OtpService
└── util/          # JwtUtil
```
