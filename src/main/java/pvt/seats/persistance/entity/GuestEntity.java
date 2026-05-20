package pvt.seats.persistance.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "guests")
public class GuestEntity extends PanacheEntityBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "email", nullable = false, length = 255)
    public String email;

    @Column(name = "full_name", length = 100)
    public String fullName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @OneToMany(mappedBy = "guest")
    public Set<BookingEntity> bookings = new LinkedHashSet<>();
}

