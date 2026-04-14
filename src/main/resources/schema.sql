CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(20)         NOT NULL CHECK (role IN ('STUDENT','ORGANIZER','ADMIN')),
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS events (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(200)  NOT NULL,
    description       TEXT,
    location          VARCHAR(255)  NOT NULL,
    event_time        TIMESTAMPTZ   NOT NULL,
    capacity          INT           NOT NULL CHECK (capacity > 0),
    available_tickets INT           NOT NULL CHECK (available_tickets >= 0),
    organizer_id      BIGINT        NOT NULL REFERENCES users(id),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_events_event_time ON events(event_time);
CREATE INDEX IF NOT EXISTS idx_events_location   ON events(location);

CREATE TABLE IF NOT EXISTS bookings (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users(id),
    event_id         BIGINT       NOT NULL REFERENCES events(id),
    ticket_count     INT          NOT NULL CHECK (ticket_count > 0),
    status           VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_booking_user_event UNIQUE (user_id, event_id)
);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id  ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_event_id ON bookings(event_id);
