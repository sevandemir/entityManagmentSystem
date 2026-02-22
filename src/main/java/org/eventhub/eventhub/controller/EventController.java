package org.eventhub.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.event.*;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.service.EventServices;
import org.eventhub.eventhub.service.FileServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor

public class EventController {

    private final EventServices eventServices;
    private final FileServices fileServices;
    private final JsonMapper.Builder builder;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto request, Principal principal) {
        // Principal.getName() artık ID dönecek şekilde ayarlı
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
            @Valid @RequestBody EventUpdateRequestDto request,
            @PathVariable Long id,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        eventServices.publishEvent(request.getEventStatus(), id, userId);
        EventResponseDto updated = eventServices.getEventById(id);  // bunu ekle
        return ResponseEntity.ok(updated);  // güncel event'i dön
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
            @Valid @RequestBody EventRequestDto request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        Event event = eventServices.updateEvent(id, request, userId);
        return ResponseEntity.ok(eventServices.getEventById(event.getId()));
    }
}
