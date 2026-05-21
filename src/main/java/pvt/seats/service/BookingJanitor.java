package pvt.seats.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import pvt.seats.persistance.entity.BookingEntity;
import pvt.seats.persistance.enums.BookingStatusEn;

@Slf4j
@ApplicationScoped
public class BookingJanitor {

    @Scheduled(every = "60s")
    @Transactional
    public void releaseExpiredReservations() {
        OffsetDateTime now = OffsetDateTime.now();

        long newlyExpiredCount = BookingEntity.update(
                "status = ?1 where status in (?2, ?3) and expiresAt < ?4",
                BookingStatusEn.EXPIRED,
                BookingStatusEn.HELD,
                BookingStatusEn.PENDING,
                now);

        long cleanedExpiredCount = BookingEntity.delete(
                "status = ?1 and expiresAt < ?2",
                BookingStatusEn.EXPIRED,
                now);

        if (newlyExpiredCount > 0 || cleanedExpiredCount > 0) {
            log.info(
                    "Booking cleanup complete: expired from held/pending={}, expired removed={}",
                    newlyExpiredCount,
                    cleanedExpiredCount);
        }
    }
}