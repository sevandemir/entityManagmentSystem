package org.eventhub.eventhub.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eventhub.eventhub.enums.EventType;

import java.time.LocalDateTime;

@Data
public class EventCreateRequestDto {
    @NotBlank(message = "Başlık boş olamaz")
    private String title;

    @NotBlank(message = "Açıklama boş olamaz")
    private String description;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private EventType eventType;

    @NotBlank(message = "Konum boş olamaz")
    private String location;

    @NotNull
    private int maxCapacity;

    @NotNull(message = "Kategori boş olamaz")
    private Long categoryId;
}
