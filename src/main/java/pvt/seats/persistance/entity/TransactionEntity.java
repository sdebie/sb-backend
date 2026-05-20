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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import pvt.seats.persistance.enums.TransactionStatusEn;

@Entity
@Table(name = "transactions")
public class TransactionEntity extends PanacheEntityBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, foreignKey = @ForeignKey(name = "transactions_booking_id_fkey"))
    public BookingEntity booking;

    @Column(name = "payfast_payment_id", length = 100)
    public String payfastPaymentId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public TransactionStatusEn status;

    @CreationTimestamp
    @Column(name = "captured_at", nullable = false, updatable = false)
    public OffsetDateTime capturedAt;
}
