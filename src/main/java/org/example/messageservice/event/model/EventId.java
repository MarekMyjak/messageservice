package org.example.messageservice.event.model;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public record EventId(UUID value) {

    public EventId {
        checkNotNull(value, "eventId");
    }

    public static EventId eventId(UUID value) {
        return new EventId(value);
    }
}
