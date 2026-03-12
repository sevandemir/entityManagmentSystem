package org.eventhub.eventhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.event.EventCreateRequestDto;
import org.eventhub.eventhub.dto.event.EventResponseDto;
import org.eventhub.eventhub.dto.event.EventSummaryResponseDto;
import org.eventhub.eventhub.dto.event.EventUpdateRequestDto;
import org.eventhub.eventhub.entity.Category;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.exception.NotFoundException;
import org.eventhub.eventhub.mapper.EventMapper;
import org.eventhub.eventhub.repo.CategoryRepository;
import org.eventhub.eventhub.repo.EventRepository;
import org.eventhub.eventhub.repo.UserRepository;
import org.eventhub.eventhub.service.EventServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventServices {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    // Sayfa başına maksimum kayıt — DoS'a karşı sabit
    private static final int MAX_PAGE_SIZE = 20;

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventById(Long id) {
        return eventRepository.findEventDetailById(id)
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı!"));
    }

    @Override
    public EventResponseDto createEvent(EventCreateRequestDto dto, Long userId) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Kategori bulunamadı!"));

        User organizer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Organizatör bulunamadı!"));

        Event event = eventMapper.toEntity(dto);
        event.setCategory(category);
        event.setOrganizer(organizer);
        eventRepository.save(event);
        return eventMapper.toResponseDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponseDto> getAllEventsSummary() {
        return eventRepository.findAllEventsSummary();
    }

    @Override
    public void updateEventImage(Long id, String fileName, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı!"));

        checkOwnership(event, userId);
        event.setImagePath(fileName);
        eventRepository.save(event);
    }

    @Override
    public EventResponseDto publishEvent(EventStatus status, Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event bulunamadı"));

        checkOwnership(event, userId);
        event.setEventStatus(status);
        eventRepository.save(event);
        return eventMapper.toResponseDto(event);
    }

    @Override
    public EventResponseDto updateEvent(Long eventId, EventUpdateRequestDto request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event bulunamadı"));

        checkOwnership(event, userId);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Kategori bulunamadı"));
            event.setCategory(category);
        }

        eventMapper.updateEntityFromDto(request, event);
        eventRepository.save(event);
        return eventMapper.toResponseDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponseDto> searchEvents(
            String search,
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        /*
         * GÜVENLİK: Search input temizleme
         *
         * Native query'de LIKE kullanıyoruz. Spring parametre binding
         * SQL injection'ı zaten önlüyor (? placeholder ile).
         *
         * Ama LIKE wildcard karakterleri (%, _) kullanıcı input'unda
         * gelirse sorgu beklenmedik şekilde çalışabilir:
         *   search = "%" → tüm kayıtları getirir
         *   search = "___" → 3 karakterli tüm başlıkları getirir
         *
         * Bu yüzden % ve _ karakterlerini escape ediyoruz.
         */
        String searchPattern = null;
        if (search != null && !search.isBlank()) {
            // LIKE wildcard karakterlerini escape et
            String sanitized = search
                    .replace("\\", "\\\\") // önce backslash
                    .replace("%", "\\%")   // sonra %
                    .replace("_", "\\_");  // sonra _
            searchPattern = "%" + sanitized.toLowerCase() + "%";
        }

        // Sayfa boyutunu zorla — kullanıcı 1000 isteyemez
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize);

        return eventRepository.searchEvents(searchPattern, categoryId, startDate, endDate, pageable)
                .map(p -> new EventSummaryResponseDto(
                        p.getId(),
                        p.getTitle(),
                        p.getStartTime(),
                        EventStatus.valueOf(p.getEventStatus()),
                        p.getLocation(),
                        p.getCategoryName(),
                        p.getImagePath()
                ));
    }

    @Override
    public void deleteEvent(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Etkinlik bulunamadı!"));

        checkOwnership(event, userId);
        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponseDto> getOrganizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    /**
     * Sahiplik kontrolü — tek yerden yönetilir.
     * Organizatör sadece kendi etkinliğine dokunabilir.
     * Admin bu metodu bypass eder (@PreAuthorize seviyesinde yönetilir).
     */
    private void checkOwnership(Event event, Long userId) {
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu işlemi yapmak için yetkiniz yok!");
        }
    }
}