package org.eventhub.eventhub.dto.event;

import org.eventhub.eventhub.enums.EventStatus;

import java.time.LocalDateTime;

public record EventSummaryResponseDto(Long id,
                                      String title,
                                      LocalDateTime startTime,
                                      EventStatus eventStatus,
                                      String location,
                                      String categoryName,
                                      String imagePath) {
}
