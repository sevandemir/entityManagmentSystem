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

    /**
     * Etkinlik oluştur — sadece ORGANIZER
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody EventCreateRequestDto request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return new ResponseEntity<>(eventServices.createEvent(request, userId), HttpStatus.CREATED);
    }

    /**
     * Tüm etkinlikler — herkese açık (SecurityConfig'de permitAll)
     */
    @GetMapping
    public ResponseEntity<List<EventSummaryResponseDto>> getAllEvents() {
        return ResponseEntity.ok(eventServices.getAllEventsSummary());
    }

    /**
     * Etkinlik detayı — herkese açık
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventServices.getEventById(id));
    }

    /**
     * Etkinlik durumu değiştir (PUBLISH / CANCEL) — ORGANIZER veya ADMIN
     * ADMIN her etkinliği yönetebilir, ORGANIZER sadece kendi etkinliğini.
     * Sahiplik kontrolü service katmanında yapılıyor.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> changeEventStatus(
            @Valid @RequestBody EventPublishRequestDto request,
            @PathVariable Long id,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(eventServices.publishEvent(request.getEventStatus(), id, userId));
    }

    /**
     * Etkinlik sil — ORGANIZER veya ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        eventServices.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Etkinlik görseli yükle — sadece ORGANIZER (kendi etkinliği)
     * Dosya validasyonu FileServices içinde yapılıyor (tip, boyut, path traversal)
     */
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventMessageDto> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {
        Long userId = Long.valueOf(principal.getName());

        String fileName = fileServices.saveImage(file);
        eventServices.updateEventImage(id, fileName, userId);

        return ResponseEntity.ok(new EventMessageDto<>(true, "Resim yüklendi", fileName));
    }

    /**
     * Etkinlik güncelle — ORGANIZER veya ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequestDto request,
            Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(eventServices.updateEvent(id, request, userId));
    }

    /**
     * Etkinlik arama — herkese açık
     * Sayfa boyutu service katmanında MAX_PAGE_SIZE=20 ile sınırlandırılıyor
     */
    @GetMapping("/search")
    public ResponseEntity<Page<EventSummaryResponseDto>> searchEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
                eventServices.searchEvents(search, categoryId, startDate, endDate, page, size)
        );
    }

    /**
     * Organizatörün kendi etkinlikleri — sadece ORGANIZER
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventSummaryResponseDto>> getMyEvents(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(eventServices.getOrganizerEvents(userId));
    }
}