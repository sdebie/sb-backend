package pvt.seats.persistance.dto;

/**
 * Data Transfer Object for updating a booking's status.
 * Used to accept status change requests in the REST API.
 */
public class UpdateBookingStatusDto {
    public String status;

    public UpdateBookingStatusDto() {}

    public UpdateBookingStatusDto(String status) {
        this.status = status;
    }
}

