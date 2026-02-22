package org.eventhub.eventhub.service;

import org.eventhub.eventhub.dto.event.EventRequestDto;
import org.eventhub.eventhub.dto.event.EventResponseDto;
import org.eventhub.eventhub.dto.event.EventSummaryResponseDto;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.enums.EventType;

import java.util.List;


public interface EventServices {
    EventResponseDto createEvent(EventRequestDto dto, Long userId);
    List<EventSummaryResponseDto> getAllEventsSummary();
    void updateStatus(Long eventId, EventStatus newStatus, Long userId);
    void updateEventImage(Long id, String fileName, Long userId);
    EventResponseDto getEventById(Long id);
    void deleteEvent(Long id, Long userId);
    void publishEvent(EventStatus status, Long eventId, Long userId);
    Event updateEvent(Long eventId, EventRequestDto request, Long userId);
}
