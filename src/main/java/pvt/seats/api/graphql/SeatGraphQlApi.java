package pvt.seats.api.graphql;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import pvt.seats.persistance.dto.SeatStatusDto;
import pvt.seats.persistance.entity.BookingEntity;
import pvt.seats.persistance.entity.SeatEntity;
import pvt.seats.persistance.enums.BookingStatusEn;
import pvt.seats.persistance.enums.SeatBookingStatusEn;

@GraphQLApi
@ApplicationScoped
public class SeatGraphQlApi {

    @Query("seatStatusList")
    public List<SeatStatusDto> seatStatusList() {
        List<SeatEntity> seats = SeatEntity.list("order by seatRow asc, seatNumber asc");
        Map<UUID, BookingEntity> latestBookingBySeat = new HashMap<>();

        List<BookingEntity> bookings = BookingEntity.listAll();
        for (BookingEntity booking : bookings) {
            if (booking.seat == null || booking.seat.id == null) {
                continue;
            }
            // Skip HELD bookings that have already expired
            if (booking.status == BookingStatusEn.HELD
                    && booking.expiresAt != null
                    && booking.expiresAt.isBefore(OffsetDateTime.now())) {
                continue;
            }

            BookingEntity current = latestBookingBySeat.get(booking.seat.id);
            if (current == null || isAfter(booking, current)) {
                latestBookingBySeat.put(booking.seat.id, booking);
            }
        }

        return seats.stream()
                .map(seat -> new SeatStatusDto(
                        seat.id,
                        seat.seatRow,
                        seat.seatNumber,
                        seat.handicapAccessible,
                        resolveStatus(latestBookingBySeat.get(seat.id)),
                        seat.seatType,
                        seat.price
                ))
                .sorted(Comparator
                        .comparing((SeatStatusDto dto) -> dto.seatRow)
                        .thenComparing(dto -> dto.seatNumber))
                .toList();
    }

    private static boolean isAfter(BookingEntity candidate, BookingEntity reference) {
        if (candidate.createdAt == null) {
            return false;
        }
        if (reference.createdAt == null) {
            return true;
        }
        return candidate.createdAt.isAfter(reference.createdAt);
    }

    private static SeatBookingStatusEn resolveStatus(BookingEntity booking) {
        if (booking == null || booking.status == null) {
            return SeatBookingStatusEn.OPEN;
        }

        BookingStatusEn status = booking.status;
        return switch (status) {
            case HELD -> SeatBookingStatusEn.HELD;
            case PENDING -> SeatBookingStatusEn.PENDING;
            case CONFIRMED -> SeatBookingStatusEn.CONFIRMED;
            case EXPIRED, CANCELLED -> SeatBookingStatusEn.OPEN;
        };
    }

    @Mutation("updateBookingStatus")
    @Transactional
    public UpdateBookingStatusResponse updateBookingStatus(UUID bookingId, String status) {
        BookingEntity booking = BookingEntity.findById(bookingId);
        if (booking == null) {
            return new UpdateBookingStatusResponse(null, "FAILED", "Booking not found.");
        }

        BookingStatusEn newStatus;
        try {
            newStatus = BookingStatusEn.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new UpdateBookingStatusResponse(null, "FAILED", "Invalid status: " + status);
        }

        // Validate status transitions
        if ((booking.status == BookingStatusEn.CANCELLED || booking.status == BookingStatusEn.EXPIRED)
                && newStatus != BookingStatusEn.CANCELLED && newStatus != BookingStatusEn.EXPIRED) {
            return new UpdateBookingStatusResponse(null, "FAILED",
                    "Cannot transition from " + booking.status + " to " + newStatus);
        }

        booking.status = newStatus;

        // When moving to HELD or PENDING, reset the expiry to 10 minutes from now
        if (newStatus == BookingStatusEn.HELD || newStatus == BookingStatusEn.PENDING) {
            booking.expiresAt = OffsetDateTime.now().plusMinutes(10);
        }

        booking.persistAndFlush();

        return new UpdateBookingStatusResponse(booking.id, "SUCCESS", null);
    }

    public record UpdateBookingStatusResponse(UUID bookingId, String result, String error) {}

}

