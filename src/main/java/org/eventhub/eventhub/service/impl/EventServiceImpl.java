package org.eventhub.eventhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.event.EventRequestDto;
import org.eventhub.eventhub.dto.event.EventResponseDto;
import org.eventhub.eventhub.dto.event.EventSummaryResponseDto;
import org.eventhub.eventhub.entity.Category;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.repo.CategoryRepository;
import org.eventhub.eventhub.repo.EventRepository;
import org.eventhub.eventhub.repo.UserRepository;
import org.eventhub.eventhub.service.EventServices;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // Sınıf seviyesinde transactional ile güvenliği sağlıyoruz
public class EventServiceImpl implements EventServices {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public EventResponseDto createEvent(EventRequestDto dto, Long userId) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı!"));

        User organizer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Organizatör bulunamadı!"));

        // Entity Mapping (Create için hala entity nesnesine ihtiyacımız var)
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setEventType(dto.getEventType());
        event.setLocation(dto.getLocation());
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setCategory(category);
        event.setOrganizer(organizer);

        Event savedEvent = eventRepository.save(event);

        // Kayıt sonrası detayı tekrar çekerek DTO dönüyoruz (Tutarlılık için)
        return getEventById(savedEvent.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponseDto> getAllEventsSummary() {
        // Repo'daki yeni Constructor Projection metodunu kullanıyoruz
        // Stream ve manuel mapping'e veda ettik!
        return eventRepository.findAllEventsSummary();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventById(Long id) {
        // Repo'daki optimize edilmiş detay sorgusunu kullanıyoruz
        return eventRepository.findEventDetailById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı!"));
    }

    @Override
    @Transactional
    public void updateStatus(Long eventId, EventStatus newStatus, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı!"));

        // GÜVENLİK: Sadece sahibi durum değiştirebilir
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new RuntimeException("Bu etkinlik üzerinde işlem yapma yetkiniz yok!");
        }

        event.setEventStatus(newStatus);
        eventRepository.save(event);
    }

    @Override
    public void updateEventImage(Long id, String fileName, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı!"));

        checkOwnership(event, userId);
        event.setImagePath(fileName);
        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı!"));

        // Güvenlik Kontrolü: Sadece etkinliği oluşturan silebilir
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new RuntimeException("Bu işlemi yapmak için yetkiniz yok!");
        }

        eventRepository.delete(event);
    }

    @Override
    public void publishEvent(EventStatus status, Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event bulunamadı"));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu etkinliği güncelleme yetkiniz yok");
        }

        event.setEventStatus(status);
        eventRepository.save(event);
    }

    private void checkOwnership(Event event, Long userId) {
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new RuntimeException("Bu etkinlik üzerinde işlem yapma yetkiniz yok!");
        }
    }

    @Override
    public Event updateEvent(Long eventId, EventRequestDto request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event bulunamadı"));

        // Sadece sahibi güncelleyebilsin
        if (!event.getOrganizer().getId().equals(userId)) {
            throw new AccessDeniedException("Bu etkinliği güncelleme yetkiniz yok");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setEventType(request.getEventType());
        event.setMaxCapacity(request.getMaxCapacity());
        // categoryId varsa onu da set et

        return eventRepository.save(event);
    }
}