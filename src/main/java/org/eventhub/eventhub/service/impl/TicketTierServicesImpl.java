package org.eventhub.eventhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.ticketTier.TicketTierCreateRequest;
import org.eventhub.eventhub.dto.ticketTier.TicketTierResponse;
import org.eventhub.eventhub.entity.Event;
import org.eventhub.eventhub.entity.TicketTier;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.enums.EventStatus;
import org.eventhub.eventhub.exception.BusinessException;
import org.eventhub.eventhub.mapper.TicketTierMapper;
import org.eventhub.eventhub.repo.EventRepository;
import org.eventhub.eventhub.repo.TicketTierRepository;
import org.eventhub.eventhub.service.TicketTierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TicketTierServicesImpl  implements TicketTierService {
    private final TicketTierRepository ticketTierRepository;
    private final EventRepository eventRepository;
    private final TicketTierMapper ticketTierMapper;

    public List<TicketTierResponse> getTiersByEvent(Long eventId) {
        return ticketTierRepository.findByEventId(eventId)
                .stream()
                .map(ticketTierMapper::toResponse)
                .toList();
    }

    @Transactional
    public TicketTierResponse addTier(Long eventId, TicketTierCreateRequest request, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("Etkinlik bulunamadı"));

        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bu etkinliğe bilet ekleyemezsiniz");
        }

        if (event.getEventStatus() == EventStatus.CANCELLED ||
                event.getEventStatus() == EventStatus.COMPLETED) {
            throw new BusinessException("Bu etkinliğe bilet eklenemez");
        }

        validateTierDates(request, event);
        checkCapacityLimit(event, request.getTotalQuantity());
        TicketTier tier = ticketTierMapper.toEntity(request);
        tier.setEvent(event);
        ticketTierRepository.save(tier);
        return ticketTierMapper.toResponse(tier);
    }

    @Transactional
    public TicketTierResponse updateTier(Long tierId, TicketTierCreateRequest request, User currentUser) {
        TicketTier tier = ticketTierRepository.findById(tierId)
                .orElseThrow(() -> new BusinessException("Bilet türü bulunamadı"));

        if (!tier.getEvent().getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bu bilet türünü düzenleyemezsiniz");
        }

        if (request.getTotalQuantity() < tier.getSoldCount()) {
            throw new BusinessException(
                    "Toplam adet, satılan bilet sayısından (" + tier.getSoldCount() + ") az olamaz"
            );
        }

        validateTierDates(request, tier.getEvent());

        ticketTierMapper.updateEntityFromDto(request, tier); // manuel setter'lar gitti
        ticketTierRepository.save(tier);
        return ticketTierMapper.toResponse(tier);
    }

    @Transactional
    public void deleteTier(Long tierId, User currentUser) {
        TicketTier tier = ticketTierRepository.findById(tierId)
                .orElseThrow(() -> new BusinessException("Bilet türü bulunamadı"));

        if (!tier.getEvent().getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bu bilet türünü silemezsiniz");
        }

        if (tier.getSoldCount() > 0) {
            throw new BusinessException("Satış yapılmış bilet türü silinemez");
        }

        ticketTierRepository.delete(tier);
    }

    // --- VALIDATION ---

    private void validateTierDates(TicketTierCreateRequest request, Event event) {
        if (!request.getSaleEndDate().isAfter(request.getSaleStartDate())) {
            throw new BusinessException("Satış bitiş tarihi, başlangıçtan sonra olmalı");
        }
        if (!request.getSaleEndDate().isBefore(event.getStartTime())) {
            throw new BusinessException("Satış bitiş tarihi, etkinlik başlangıcından önce olmalı");
        }
    }

    private void checkCapacityLimit(Event event, int newTierQuantity) {
        int currentTotal = ticketTierRepository.sumTotalQuantityByEventId(event.getId());

        if (currentTotal + newTierQuantity > event.getMaxCapacity()) {
            int remaining = event.getMaxCapacity() - currentTotal;
            throw new BusinessException("Kapasite aşılıyor! Kalan kapasite: " + remaining);
        }
    }
}
