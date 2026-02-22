package org.eventhub.eventhub.repo;

import jakarta.persistence.LockModeType;
import org.eventhub.eventhub.entity.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTierRepository extends JpaRepository<TicketTier,Long> {
    List<TicketTier> findByEventId(Long eventId);

    // Kapasite kontrolü ve eş zamanlı alımları yönetmek için kilitleme (Pessimistic Lock)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketTier t WHERE t.id = :id")
    Optional<TicketTier> findByIdWithLock(Long id);
}
