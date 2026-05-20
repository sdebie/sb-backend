package pvt.seats.persistance.dto;

import java.util.UUID;
import pvt.seats.persistance.enums.SeatBookingStatusEn;

public class SeatStatusDto {

    public UUID seatId;
    public String seatRow;
    public Integer seatNumber;
    public Boolean handicapAccessible;
    public SeatBookingStatusEn bookingStatus;

    public SeatStatusDto() {
    }

    public SeatStatusDto(
            UUID seatId,
            String seatRow,
            Integer seatNumber,
            Boolean handicapAccessible,
            SeatBookingStatusEn bookingStatus
    ) {
        this.seatId = seatId;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.handicapAccessible = handicapAccessible;
        this.bookingStatus = bookingStatus;
    }
}

