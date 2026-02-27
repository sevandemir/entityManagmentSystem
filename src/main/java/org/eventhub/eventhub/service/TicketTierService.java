package org.eventhub.eventhub.service;


import org.eventhub.eventhub.dto.ticketTier.TicketTierCreateRequest;
import org.eventhub.eventhub.dto.ticketTier.TicketTierResponse;
import org.eventhub.eventhub.entity.User;

import java.util.List;

public interface TicketTierService {

    List<TicketTierResponse> getTiersByEvent(Long eventId);

    TicketTierResponse addTier(Long eventId, TicketTierCreateRequest request, User currentUser);

    TicketTierResponse updateTier(Long tierId, TicketTierCreateRequest request, User currentUser);

    void deleteTier(Long tierId, User currentUser);
}