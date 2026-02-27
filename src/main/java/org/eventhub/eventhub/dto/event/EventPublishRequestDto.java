package org.eventhub.eventhub.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eventhub.eventhub.enums.EventStatus;

@Data
public class EventPublishRequestDto {

    @NotNull
    private EventStatus eventStatus;
}
