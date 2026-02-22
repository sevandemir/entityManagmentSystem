package org.eventhub.eventhub.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eventhub.eventhub.enums.EventType;

import java.time.LocalDateTime;

@Data
public class EventRequestDto {
    @NotBlank(message = "title cannot be empty")
    private String title;

    @NotBlank(message = "title cannot be empty")
    private String description;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private EventType eventType;

    @NotBlank(message = "title cannot be empty")
    private String location;

    @NotNull
    private int maxCapacity;

    @NotNull(message = "category cannot be empty")
    private Long categoryId;
}
