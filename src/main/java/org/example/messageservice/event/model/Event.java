package org.example.messageservice.event.model;


import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.*;
import static java.util.UUID.randomUUID;
import static org.example.messageservice.event.model.EventId.eventId;
import static org.example.messageservice.event.model.EventState.CREATED;
import static org.example.messageservice.event.model.EventState.PUBLISHED;

public record Event(
    EventId id,
    EventState state,
    EventType type,
    JsonNode payload,
    Instant createdDate,
    Optional<Instant> publishedDate
) {
    public Event {
        checkNotNull(id, "id");
        checkNotNull(state, "state");
        checkNotNull(type, "type");
        checkNotNull(payload, "payload");
        checkNotNull(createdDate, "createdDate");
        checkNotNull(publishedDate, "publishedDate");

        if (state == EventState.PUBLISHED) {
            checkArgument(publishedDate.isPresent(), "PublishedDate is required in PUBLISHED state");
        }
    }

    public Event publish(Instant publishedDate) {
        if (state == PUBLISHED) {
            checkArgument(publishedDate == this.publishedDate.get(), "Argument publishedDate have to be the same as existing publishedDate");
            return this;
        }

        checkState(state == CREATED, "Expected state %s but was %s", CREATED, state);
        return copy()
            .state(PUBLISHED)
            .publishedDate(Optional.of(publishedDate))
            .build();
    }

    private Builder copy() {
        return builder()
            .id(id)
            .state(state)
            .type(type)
            .payload(payload)
            .createdDate(createdDate)
            .publishedDate(publishedDate);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private EventId id = eventId(randomUUID());
        private EventState state = CREATED;
        private EventType type;
        private JsonNode payload;
        private Instant createdDate;
        private Optional<Instant> publishedDate = Optional.empty();

        public Builder id(EventId id) {
            this.id = id;
            return this;
        }

        public Builder state(EventState state) {
            this.state = state;
            return this;
        }

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder payload(JsonNode payload) {
            this.payload = payload;
            return this;
        }

        public Builder createdDate(Instant createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder publishedDate(Optional<Instant> publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Event build() {
            return new Event(
                id,
                state,
                type,
                payload,
                createdDate,
                publishedDate
            );
        }
    }
}
