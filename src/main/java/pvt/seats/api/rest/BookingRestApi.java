package pvt.seats.api.rest;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.UUID;
import pvt.seats.persistance.dto.HoldSeatRequestDto;
import pvt.seats.persistance.dto.UpdateBookingStatusDto;
import pvt.seats.persistance.entity.BookingEntity;
import pvt.seats.persistance.entity.GuestEntity;
import pvt.seats.persistance.entity.SeatEntity;
import pvt.seats.persistance.enums.BookingStatusEn;

@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingRestApi {

    /**
     * Temporarily hold a seat for a guest while they are on the seat selection page.
     * A HELD booking expires automatically after 10 minutes.
     * Returns 201 with the booking ID on success, or 409 if the seat is already held/pending/confirmed.
     */
    @POST
    @Path("/hold")
    @Transactional
    public Response holdSeat(HoldSeatRequestDto request) {
        if (request == null || request.email == null || request.seatId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("email and seatId are required.")
                    .build();
        }

        SeatEntity seat = SeatEntity.findById(request.seatId);
        if (seat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Seat not found.")
                    .build();
        }

        // Find or create guest by email
        GuestEntity guest = GuestEntity.find("email", request.email.trim().toLowerCase())
                .<GuestEntity>firstResultOptional()
                .orElseGet(() -> {
                    GuestEntity newGuest = new GuestEntity();
                    newGuest.email = request.email.trim().toLowerCase();
                    newGuest.persist();
                    return newGuest;
                });

        // Check that no active (HELD / PENDING / CONFIRMED) booking already exists for this seat
        boolean alreadyActive = BookingEntity
                .find("seat.id = ?1 and status in ?2 and (expiresAt > ?3 or status in ('PENDING', 'CONFIRMED'))",
                        seat.id,
                        java.util.List.of(BookingStatusEn.HELD, BookingStatusEn.PENDING, BookingStatusEn.CONFIRMED),
                        OffsetDateTime.now())
                .count() > 0;

        if (alreadyActive) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Seat is already reserved.")
                    .build();
        }

        BookingEntity booking = new BookingEntity();
        booking.guest = guest;
        booking.seat = seat;
        booking.status = BookingStatusEn.HELD;
        booking.expiresAt = OffsetDateTime.now().plusMinutes(10);

        try {
            booking.persistAndFlush();
            return Response.status(Response.Status.CREATED)
                    .entity(new HoldResponse(booking.id))
                    .build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Seat is already reserved.")
                    .build();
        }
    }

    /**
     * Release a HELD booking when the guest deselects a seat or leaves the page.
     */
    @DELETE
    @Path("/hold/{bookingId}")
    @Transactional
    public Response releaseHold(@PathParam("bookingId") UUID bookingId) {
        BookingEntity booking = BookingEntity.findById(bookingId);
        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Booking not found.")
                    .build();
        }

        if (booking.status != BookingStatusEn.HELD) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Only HELD bookings can be released this way.")
                    .build();
        }

        booking.status = BookingStatusEn.CANCELLED;
        booking.persistAndFlush();

        return Response.noContent().build();
     }

     /**
      * Update the status of a booking.
      * Allows transitions between booking statuses (e.g., HELD → PENDING → CONFIRMED, or any status → CANCELLED).
      * Returns 200 with the updated booking on success, or 404/400/409 on failure.
      */
     @PUT
     @Path("/{bookingId}/status")
     @Transactional
     public Response updateBookingStatus(@PathParam("bookingId") UUID bookingId, UpdateBookingStatusDto request) {
         if (request == null || request.status == null) {
             return Response.status(Response.Status.BAD_REQUEST)
                     .entity("status is required.")
                     .build();
         }

         BookingEntity booking = BookingEntity.findById(bookingId);
         if (booking == null) {
             return Response.status(Response.Status.NOT_FOUND)
                     .entity("Booking not found.")
                     .build();
         }

         BookingStatusEn newStatus;
         try {
             newStatus = BookingStatusEn.valueOf(request.status.toUpperCase());
         } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.BAD_REQUEST)
                     .entity("Invalid status: " + request.status)
                     .build();
         }

         // Optional: Validate status transitions
         // For now, allow any transition (you can add validation logic here)
         // Example: Don't allow transitioning from CANCELLED or EXPIRED to other statuses
         if ((booking.status == BookingStatusEn.CANCELLED || booking.status == BookingStatusEn.EXPIRED)
                 && newStatus != BookingStatusEn.CANCELLED && newStatus != BookingStatusEn.EXPIRED) {
             return Response.status(Response.Status.CONFLICT)
                     .entity("Cannot transition from " + booking.status + " to " + newStatus)
                     .build();
         }

         booking.status = newStatus;
         booking.persistAndFlush();

         return Response.ok(new BookingStatusResponse(booking.id, booking.status.name())).build();
     }

     /** Minimal response body carrying the new booking ID back to the frontend. */
     public record HoldResponse(UUID bookingId) {}

     /** Response body for booking status update. */
     public record BookingStatusResponse(UUID bookingId, String status) {}
 }

