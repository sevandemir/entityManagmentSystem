package org.eventhub.eventhub.repo;

import org.eventhub.eventhub.dto.event.EventResponseDto;
import org.eventhub.eventhub.dto.event.EventSummaryResponseDto;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.service.EventSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventStatus(EventStatus eventStatus);

    @Query("""
    SELECT new org.eventhub.eventhub.dto.event.EventSummaryResponseDto(
        e.id, e.title, e.startTime, e.eventStatus, e.location, e.category.name, e.imagePath
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


    // Kategoriye göre filtreleme
    List<Event> findByCategoryId(Long categoryId);


    @Query(value = """
    SELECT e.id, e.title, e.start_time as startTime, e.status as eventStatus, 
           e.location, c.name as categoryName, e.image_path as imagePath
    FROM events e
    JOIN categories c ON c.id = e.category_id
    WHERE e.status = 'PUBLISHED'
    AND (CAST(:searchPattern AS text) IS NULL OR LOWER(e.title) LIKE :searchPattern OR LOWER(e.description) LIKE :searchPattern)
    AND (CAST(:categoryId AS bigint) IS NULL OR c.id = :categoryId)
    AND (CAST(:startDate AS timestamp) IS NULL OR e.start_time >= CAST(:startDate AS timestamp))
    AND (CAST(:endDate AS timestamp) IS NULL OR e.start_time <= CAST(:endDate AS timestamp))
    """,
            countQuery = """
    SELECT COUNT(*) FROM events e
    JOIN categories c ON c.id = e.category_id
    WHERE e.status = 'PUBLISHED'
    AND (CAST(:searchPattern AS text) IS NULL OR LOWER(e.title) LIKE :searchPattern OR LOWER(e.description) LIKE :searchPattern)
    AND (CAST(:categoryId AS bigint) IS NULL OR c.id = :categoryId)
    AND (CAST(:startDate AS timestamp) IS NULL OR e.start_time >= CAST(:startDate AS timestamp))
    AND (CAST(:endDate AS timestamp) IS NULL OR e.start_time <= CAST(:endDate AS timestamp))
    """,
            nativeQuery = true)
    Page<EventSummaryProjection> searchEvents(
            @Param("searchPattern") String searchPattern,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Organizatörün kendi etkinlikleri (dashboard için)
    @Query("""
            SELECT new org.eventhub.eventhub.dto.event.EventSummaryResponseDto(
                e.id, e.title, e.startTime, e.eventStatus, e.location, c.name, e.imagePath
            )
            FROM Event e
            JOIN e.category c
            WHERE e.organizer.id = :organizerId
            """)
    List<EventSummaryResponseDto> findByOrganizerId(@Param("organizerId") Long organizerId);
}
