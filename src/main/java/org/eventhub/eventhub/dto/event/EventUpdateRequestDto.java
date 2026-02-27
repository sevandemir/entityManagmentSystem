package org.eventhub.eventhub.dto.event;

import lombok.Data;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.enums.EventType;

import java.time.LocalDateTime;

@Data
public class EventUpdateRequestDto {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventType eventType;
    private String location;
    private Integer maxCapacity;
    private Long categoryId;
}
