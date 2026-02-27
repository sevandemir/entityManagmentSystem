package org.eventhub.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventhub.eventhub.dto.ticketTier.TicketTierCreateRequest;
import org.eventhub.eventhub.dto.ticketTier.TicketTierResponse;
import org.eventhub.eventhub.entity.User;
import org.eventhub.eventhub.service.TicketTierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events/{eventId}/ticket-tiers")
@RequiredArgsConstructor

public class TicketTierController{
    private final TicketTierService ticketTierService;

    @GetMapping
    public ResponseEntity<List<TicketTierResponse>> getTiers(@PathVariable Long eventId) {
        return ResponseEntity.ok(ticketTierService.getTiersByEvent(eventId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTierResponse> addTier(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTierCreateRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketTierService.addTier(eventId, request, currentUser));
    }

    @PutMapping("/{tierId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTierResponse> updateTier(
            @PathVariable Long eventId,
            @PathVariable Long tierId,
            @Valid @RequestBody TicketTierCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ticketTierService.updateTier(tierId, request, currentUser));
    }

    @DeleteMapping("/{tierId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteTier(
            @PathVariable Long eventId,
            @PathVariable Long tierId,
            @AuthenticationPrincipal User currentUser) {
        ticketTierService.deleteTier(tierId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
