-- Seed guests used by sample bookings
INSERT INTO guests (id, email, full_name)
VALUES
    (uuid_generate_v5(uuid_ns_url(), 'guest-alice'), 'alice@example.com', 'Alice Johnson'),
    (uuid_generate_v5(uuid_ns_url(), 'guest-bob'), 'bob@example.com', 'Bob Smith'),
    (uuid_generate_v5(uuid_ns_url(), 'guest-carol'), 'carol@example.com', 'Carol Davis'),
    (uuid_generate_v5(uuid_ns_url(), 'guest-dan'), 'dan@example.com', 'Dan Miller');

-- Seed seats A1 through J10 with deterministic UUIDs so bookings can reference them
INSERT INTO seats (id, seat_row, seat_number, is_handicap_accessible)
SELECT
    uuid_generate_v5(uuid_ns_url(), format('seat-%s-%s', chr(row_num), seat_num)) AS id,
    chr(row_num) AS seat_row,
    seat_num,
    false AS is_handicap_accessible
FROM
    generate_series(65, 80) AS row_num,
    generate_series(1, 26) AS seat_num;

-- Sample bookings to exercise the seat status endpoint
INSERT INTO bookings (id, guest_id, seat_id, status, created_at, expires_at)
VALUES
    (
        uuid_generate_v5(uuid_ns_url(), 'booking-a1-confirmed'),
        uuid_generate_v5(uuid_ns_url(), 'guest-alice'),
        uuid_generate_v5(uuid_ns_url(), 'seat-A-1'),
        'CONFIRMED',
        CURRENT_TIMESTAMP - INTERVAL '15 minutes',
        CURRENT_TIMESTAMP + INTERVAL '15 minutes'
    ),
    (
        uuid_generate_v5(uuid_ns_url(), 'booking-a2-pending'),
        uuid_generate_v5(uuid_ns_url(), 'guest-bob'),
        uuid_generate_v5(uuid_ns_url(), 'seat-A-2'),
        'PENDING',
        CURRENT_TIMESTAMP - INTERVAL '5 minutes',
        CURRENT_TIMESTAMP + INTERVAL '10 minutes'
    ),
    (
        uuid_generate_v5(uuid_ns_url(), 'booking-b1-expired'),
        uuid_generate_v5(uuid_ns_url(), 'guest-carol'),
        uuid_generate_v5(uuid_ns_url(), 'seat-B-1'),
        'EXPIRED',
        CURRENT_TIMESTAMP - INTERVAL '45 minutes',
        CURRENT_TIMESTAMP - INTERVAL '15 minutes'
    ),
    (
        uuid_generate_v5(uuid_ns_url(), 'booking-b2-cancelled'),
        uuid_generate_v5(uuid_ns_url(), 'guest-dan'),
        uuid_generate_v5(uuid_ns_url(), 'seat-B-2'),
        'CANCELLED',
        CURRENT_TIMESTAMP - INTERVAL '20 minutes',
        CURRENT_TIMESTAMP + INTERVAL '5 minutes'
    );
