package org.example.messageservice;

import org.example.messageservice.event.model.Event;
import tools.jackson.databind.ObjectMapper;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.example.messageservice.event.model.EventId.eventId;
import static org.example.messageservice.event.model.EventState.CREATED;
import static org.example.messageservice.event.model.EventType.eventType;

public interface EventTestData {

    static Event.Builder aEvent() {
        return Event.builder()
            .id(eventId(randomUUID()))
            .state(CREATED)
            .type(eventType("org.example.messageservice.message.MessageCreatedEvent"))
            .payload(new ObjectMapper().createObjectNode().put("messageId", randomUUID().toString()))
            .createdDate(now());
    }
}
