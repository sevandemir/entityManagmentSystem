package org.eventhub.eventhub.repo;

import org.eventhub.eventhub.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration,Long> {
    // Kullanıcı bu etkinliğe zaten kayıtlı mı?
    boolean existsByAttendeeIdAndEventId(Long attendeeId, Long eventId);

    // QR Kod ile check-in için bilet bulma
    Optional<Registration> findByQrCodeUuid(String qrCodeUuid);

    // Kullanıcının kendi biletlerini görmesi
    List<Registration> findByAttendeeId(Long attendeeId);
}
