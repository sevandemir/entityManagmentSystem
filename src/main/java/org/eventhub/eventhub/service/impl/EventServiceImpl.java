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
@Transactional // Sınıf seviyesinde transactional ile güvenliği sağlıyoruz
public class EventServiceImpl implements EventServices {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;


    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventById(Long id) {
        // Repo'daki optimize edilmiş detay sorgusunu kullanıyoruz
        return eventRepository.findEventDetailById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı!"));
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
        // Repo'daki yeni Constructor Projection metodunu kullanıyoruz
        // Stream ve manuel mapping'e veda ettik!
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

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu etkinliği güncelleme yetkiniz yok");
        }

        event.setEventStatus(status);
        eventRepository.save(event);
        return eventMapper.toResponseDto(event);
    }

    private void checkOwnership(Event event, Long userId) {
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu etkinlik üzerinde işlem yapma yetkiniz yok!");
        }
    }

    @Override
    public EventResponseDto updateEvent(Long eventId, EventUpdateRequestDto request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event bulunamadı"));

        // Sadece sahibi güncelleyebilsin
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu etkinliği güncelleme yetkiniz yok");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Kategori bulunamadı"));
            event.setCategory(category);
        }

        eventMapper.updateEntityFromDto(request, event);
        eventRepository.save(event);
        return eventMapper.toResponseDto(event);
    }

    public Page<EventSummaryResponseDto> searchEvents(
            String search,
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {

        String searchPattern = null;

        if (search != null && !search.isBlank()) {
            searchPattern = "%" + search.toLowerCase() + "%";
        }

        size = 20;

        Pageable pageable = PageRequest.of(page, size);

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

        // Güvenlik Kontrolü: Sadece etkinliği oluşturan silebilir
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu işlemi yapmak için yetkiniz yok!");
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponseDto> getOrganizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }
}