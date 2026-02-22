package org.eventhub.eventhub.repo;

import org.eventhub.eventhub.dto.event.EventResponseDto;
import org.eventhub.eventhub.dto.event.EventSummaryResponseDto;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event,Long> {
    List<Event> findByEventStatus(EventStatus eventStatus);

    @Query("""
        SELECT new org.eventhub.eventhub.dto.event.EventSummaryResponseDto(
            e.id, e.title, e.startTime,e.eventStatus, e.location, e.category.name
        )
        FROM Event e
    """)
    List<EventSummaryResponseDto> findAllEventsSummary();

    @Query("""
               SELECT new org.eventhub.eventhub.dto.event.EventResponseDto(
                   e.id,
                   e.title,
                   e.description,
                   e.startTime,
                   e.endTime,
                   e.eventType,
                   e.eventStatus,
                   e.location,
                   e.maxCapacity,
                   e.category.name,
                   e.organizer.userName
            )
                   FROM Event e
                   WHERE e.id = :id
            """)
    Optional<EventResponseDto> findEventDetailById(@Param("id") Long id);

    // Organizatörün kendi etkinliklerini görmesi için
    List<Event> findByOrganizerId(Long organizerId);

    // Kategoriye göre filtreleme
    List<Event> findByCategoryId(Long categoryId);

}
