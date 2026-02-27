package org.eventhub.eventhub.service;

import org.eventhub.eventhub.dto.event.EventCreateRequestDto;
import org.eventhub.eventhub.dto.event.EventResponseDto;
import org.eventhub.eventhub.dto.event.EventSummaryResponseDto;
import org.eventhub.eventhub.dto.event.EventUpdateRequestDto;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.enums.EventStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;


public interface EventServices {
    EventResponseDto createEvent(EventCreateRequestDto dto, Long userId);
    List<EventSummaryResponseDto> getAllEventsSummary();
    void updateEventImage(Long id, String fileName, Long userId);
    EventResponseDto getEventById(Long id);
    void deleteEvent(Long id, Long userId);
    EventResponseDto  publishEvent(EventStatus status, Long eventId, Long userId);
    EventResponseDto  updateEvent(Long eventId, EventUpdateRequestDto request, Long userId);
    Page<EventSummaryResponseDto> searchEvents(
            String search,
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );

    List<EventSummaryResponseDto> getOrganizerEvents(Long organizerId);
}
