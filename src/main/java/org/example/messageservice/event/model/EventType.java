package org.example.messageservice.event.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record EventType(String value) {

    public EventType {
        checkNotNull(value, "externalId");
        checkArgument(!value.isBlank(), "EventType cannot be blank.");
        checkArgument(value.length() <= 255, "EventType maximum length is 255 characters.");
    }

    public static EventType eventType(String value) {
        return new EventType(value);
    }

    public static EventType MESSAGE_CREATED_EVENT = eventType("org.example.messageservice.message.MessageCreatedEvent");
}
