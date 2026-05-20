package pvt.seats.persistance.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import pvt.seats.persistance.enums.BookingStatusEn;

@Entity
@Table(name = "bookings")
public class BookingEntity extends PanacheEntityBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false, foreignKey = @ForeignKey(name = "bookings_guest_id_fkey"))
    public GuestEntity guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false, foreignKey = @ForeignKey(name = "bookings_seat_id_fkey"))
    public SeatEntity seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public BookingStatusEn status = BookingStatusEn.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    public OffsetDateTime expiresAt;

    @OneToMany(mappedBy = "booking")
    public Set<TransactionEntity> transactions = new LinkedHashSet<>();
}
