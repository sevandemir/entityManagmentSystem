package org.eventhub.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventhub.eventhub.dto.ticketTier.TicketTierCreateRequest;
import org.eventhub.eventhub.dto.ticketTier.TicketTierResponse;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.exception.NotFoundException;
import org.eventhub.eventhub.repo.UserRepository;
import org.eventhub.eventhub.service.TicketTierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events/{eventId}/ticket-tiers")
@RequiredArgsConstructor
public class TicketTierController {

    private final TicketTierService ticketTierService;
    private final UserRepository userRepository;

    /**
     * Etkinlik bilet türlerini listele — herkese açık
     */
    @GetMapping
    public ResponseEntity<List<TicketTierResponse>> getTiers(@PathVariable Long eventId) {
        return ResponseEntity.ok(ticketTierService.getTiersByEvent(eventId));
    }

    /**
     * Bilet türü ekle — sadece ORGANIZER
     *
     * NOT: @AuthenticationPrincipal User kullanımı kaldırıldı.
     * User entity'si UserDetails implement etmiyor, bu yüzden
     * @AuthenticationPrincipal null döndürüyor.
     * Principal → userId → User şeklinde çözüm uygulandı.
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTierResponse> addTier(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTierCreateRequest request,
            Principal principal) {

        User currentUser = getUserFromPrincipal(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketTierService.addTier(eventId, request, currentUser));
    }

    /**
     * Bilet türü güncelle — sadece ORGANIZER (kendi etkinliği)
     */
    @PutMapping("/{tierId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTierResponse> updateTier(
            @PathVariable Long eventId,
            @PathVariable Long tierId,
            @Valid @RequestBody TicketTierCreateRequest request,
            Principal principal) {

        User currentUser = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ticketTierService.updateTier(tierId, request, currentUser));
    }

    /**
     * Bilet türü sil — sadece ORGANIZER (kendi etkinliği, satış yapılmamışsa)
     */
    @DeleteMapping("/{tierId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteTier(
            @PathVariable Long eventId,
            @PathVariable Long tierId,
            Principal principal) {

        User currentUser = getUserFromPrincipal(principal);
        ticketTierService.deleteTier(tierId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT'den gelen userId ile User entity'sini çeker.
     * Principal.getName() → userId (JwtUtil.getUserIdFromToken ile set edildi)
     */
    private User getUserFromPrincipal(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));
    }
}