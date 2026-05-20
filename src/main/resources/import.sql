-- Enable the UUID extension to automatically generate IDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. GUESTS TABLE
-- Stores contact details for the person making the booking without requiring registration.
CREATE TABLE guests (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        email VARCHAR(255) NOT NULL,
                        full_name VARCHAR(100),
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. SEATS TABLE
-- Represents the physical seats available in your school venue.
CREATE TABLE seats (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       seat_row VARCHAR(5) NOT NULL,       -- e.g., 'A', 'B', 'C'
                       seat_number INT NOT NULL,           -- e.g., 1, 2, 3
                       is_handicap_accessible BOOLEAN DEFAULT FALSE,
                       UNIQUE (seat_row, seat_number)       -- Prevents duplicate seat configurations
);


-- 3. BOOKINGS TABLE
-- Manages the state of a seat reservation.
-- Includes an expiration timestamp to automatically handle abandoned PayFast checkouts.
CREATE TABLE bookings (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          guest_id UUID NOT NULL REFERENCES guests(id) ON DELETE RESTRICT,
                          seat_id UUID NOT NULL REFERENCES seats(id) ON DELETE RESTRICT,
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- HELD, PENDING, CONFIRMED, EXPIRED, CANCELLED
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          expires_at TIMESTAMP WITH TIME ZONE NOT NULL,  -- e.g., created_at + 10 minutes

    -- Ensure a seat cannot be actively tied to multiple valid bookings simultaneously.
    -- HELD is a temporary client-side hold; it joins PENDING/CONFIRMED in the active-booking guard.
                          UNIQUE (seat_id, status) WHERE (status IN ('HELD', 'PENDING', 'CONFIRMED'))
);

-- 4. TRANSACTIONS TABLE
-- Audits the payment interactions with PayFast.
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
                              payfast_payment_id VARCHAR(100),    -- The reference returned by PayFast (pf_payment_id)
                              amount NUMERIC(10, 2) NOT NULL,     -- e.g., 150.00
                              status VARCHAR(20) NOT NULL,        -- COMPLETE, FAILED, PENDING
                              captured_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance on foreign key lookups and status filtering
CREATE INDEX idx_bookings_guest ON bookings(guest_id);
CREATE INDEX idx_bookings_seat ON bookings(seat_id);
CREATE INDEX idx_bookings_status_expiry ON bookings(status, expires_at);
CREATE INDEX idx_transactions_booking ON transactions(booking_id);
-- Index the email column since you will frequently look up guests by email during checkout
CREATE INDEX idx_guests_email ON guests(email);


-- Create the new updated index including 'HELD'
CREATE UNIQUE INDEX idx_unique_active_bookings
    ON bookings (seat_id)
    WHERE (status IN ('HELD', 'PENDING', 'CONFIRMED'));