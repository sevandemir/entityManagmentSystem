package org.eventhub.eventhub.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.enums.EventType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor // 11 parametreli constructor'ı bu oluşturur
@NoArgsConstructor
public class EventResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventType eventType;
    private EventStatus eventStatus;
    private String location;
    private int maxCapacity; // İlkel tip (int) olduğuna dikkat
    private String categoryName;
    private String organizerName;
}

