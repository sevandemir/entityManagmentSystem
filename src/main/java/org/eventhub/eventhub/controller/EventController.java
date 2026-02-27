package org.eventhub.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.event.*;
import org.eventhub.eventhub.service.EventServices;
import org.eventhub.eventhub.service.FileServices;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor

public class EventController {

    private final EventServices eventServices;
    private final FileServices fileServices;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventCreateRequestDto request, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        EventResponseDto response = eventServices.createEvent(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventSummaryResponseDto>> getAllEvents() {
        return ResponseEntity.ok(eventServices.getAllEventsSummary());
    }

    // ID ile Etkinlik Getir
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventServices.getEventById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> publishEvent(
            @Valid @RequestBody EventPublishRequestDto request,
            @PathVariable Long id,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(eventServices.publishEvent(request.getEventStatus(), id, userId));  // güncel event'i dön
    }

    // Etkinlik Sil (Sadece Organizatör)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        eventServices.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventMessageDto> uploadImage(@PathVariable Long id,
                                                       @RequestParam("file") MultipartFile file,
                                                       Principal principal) throws IOException {
        Long userId = Long.valueOf(principal.getName());

        // Önce dosyayı kaydet
        String fileName = fileServices.saveImage(file);

        // Sonra DB'yi güncelle
        eventServices.updateEventImage(id, fileName, userId);

        // String yerine MessageResponse DTO dönüyoruz
        return ResponseEntity.ok(
                new EventMessageDto<>(true, "Resim yüklendi", fileName)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequestDto request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(eventServices.updateEvent(id, request, userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EventSummaryResponseDto>> searchEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(
                eventServices.searchEvents(search, categoryId, startDate, endDate, page, size)
        );
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventSummaryResponseDto>> getMyEvents(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(eventServices.getOrganizerEvents(userId));
    }
}
