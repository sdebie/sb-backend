-- Enable the UUID extension to automatically generate IDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. GUESTS TABLE
-- Stores contact details for the person making the booking without requiring registration.
CREATE TABLE if not exists guests (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        email VARCHAR(255) NOT NULL,
                        full_name VARCHAR(100),
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. SEATS TABLE
-- Represents the physical seats available in your school venue.
CREATE TABLE if not exists seats (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       seat_row VARCHAR(5) NOT NULL,       -- e.g., 'A', 'B', 'C'
                       seat_number INT NOT NULL,           -- e.g., 1, 2, 3
                       is_handicap_accessible BOOLEAN DEFAULT FALSE,
                       seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',  -- e.g., 'STANDARD', 'VIP'
                       price NUMERIC(10, 2) NOT NULL DEFAULT 150.00,        -- ticket price in ZAR
                       UNIQUE (seat_row, seat_number)       -- Prevents duplicate seat configurations
);

-- Add seat_type and price to seats if upgrading an existing DB
ALTER TABLE seats ADD COLUMN IF NOT EXISTS seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE seats ADD COLUMN IF NOT EXISTS price NUMERIC(10, 2) NOT NULL DEFAULT 150.00;


-- 3. BOOKINGS TABLE
-- Manages the state of a seat reservation.
-- Includes an expiration timestamp to automatically handle abandoned PayFast checkouts.
CREATE TABLE if not exists bookings (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          guest_id UUID NOT NULL REFERENCES guests(id) ON DELETE RESTRICT,
                          seat_id UUID NOT NULL REFERENCES seats(id) ON DELETE RESTRICT,
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 4. TRANSACTIONS TABLE
-- Audits the payment interactions with PayFast.
CREATE TABLE if not exists transactions (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE RESTRICT,
                              payfast_payment_id VARCHAR(100),    -- The reference returned by PayFast (pf_payment_id)
                              amount NUMERIC(10, 2) NOT NULL,     -- e.g., 150.00
                              status VARCHAR(20) NOT NULL,        -- COMPLETE, FAILED, PENDING
                              captured_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance on foreign key lookups and status filtering
CREATE INDEX if not exists idx_bookings_guest ON bookings(guest_id);
CREATE INDEX if not exists idx_bookings_seat ON bookings(seat_id);
CREATE INDEX if not exists idx_bookings_status_expiry ON bookings(status, expires_at);
CREATE INDEX if not exists idx_transactions_booking ON transactions(booking_id);
-- Index the email column since you will frequently look up guests by email during checkout
CREATE INDEX if not exists idx_guests_email ON guests(email);


-- Create the new updated index including 'HELD'
CREATE UNIQUE INDEX if not exists idx_unique_active_bookings
    ON bookings (seat_id)
    WHERE (status IN ('HELD', 'PENDING', 'CONFIRMED'));