package org.eventhub.eventhub.service;

import java.time.LocalDateTime;

public interface EventSummaryProjection {
    Long getId();
    String getTitle();
    LocalDateTime getStartTime();
    String getEventStatus();
    String getLocation();
    String getCategoryName();
    String getImagePath(); // ekle
}