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

        long newlyExpiredHeldCount = BookingEntity.update(
                "status = ?1 where status = ?2 and expiresAt < ?3",
                BookingStatusEn.EXPIRED,
                BookingStatusEn.HELD,
                now);

        long cleanedExpiredCount = BookingEntity.delete(
                "status = ?1 and expiresAt < ?2",
                BookingStatusEn.EXPIRED,
                now);

        if (newlyExpiredHeldCount > 0 || cleanedExpiredCount > 0) {
            log.info(
                    "Booking cleanup complete: held->expired={}, expired removed={}",
                    newlyExpiredHeldCount,
                    cleanedExpiredCount);
        }
    }
}