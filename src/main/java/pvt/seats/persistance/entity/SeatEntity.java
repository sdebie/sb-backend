package pvt.seats.persistance.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "seats")
public class SeatEntity extends PanacheEntityBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "seat_row", nullable = false, length = 5)
    public String seatRow;

    @Column(name = "seat_number", nullable = false)
    public Integer seatNumber;

    @Column(name = "is_handicap_accessible", nullable = false)
    public Boolean handicapAccessible = false;

    @OneToMany(mappedBy = "seat")
    public Set<BookingEntity> bookings = new LinkedHashSet<>();
}

