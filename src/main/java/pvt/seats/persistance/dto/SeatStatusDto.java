package pvt.seats.persistance.dto;

import java.math.BigDecimal;
import java.util.UUID;
import pvt.seats.persistance.enums.SeatBookingStatusEn;
import pvt.seats.persistance.enums.SeatTypeEn;

public class SeatStatusDto {

    public UUID seatId;
    public String seatRow;
    public Integer seatNumber;
    public Boolean handicapAccessible;
    public SeatBookingStatusEn bookingStatus;
    public SeatTypeEn seatType;
    public BigDecimal price;

    public SeatStatusDto() {
    }

    public SeatStatusDto(
            UUID seatId,
            String seatRow,
            Integer seatNumber,
            Boolean handicapAccessible,
            SeatBookingStatusEn bookingStatus,
            SeatTypeEn seatType,
            BigDecimal price
    ) {
        this.seatId = seatId;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.handicapAccessible = handicapAccessible;
        this.bookingStatus = bookingStatus;
        this.seatType = seatType;
        this.price = price;
    }
}



