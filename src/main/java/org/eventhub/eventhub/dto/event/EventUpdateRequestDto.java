package org.eventhub.eventhub.dto.event;

import lombok.Data;
import org.eventhub.eventhub.enums.EventStatus;

@Data
public class EventUpdateRequestDto {
    private Long id;
    private EventStatus eventStatus;
}
