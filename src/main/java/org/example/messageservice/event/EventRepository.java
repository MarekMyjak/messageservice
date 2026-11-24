package org.example.messageservice.event;

import org.example.messageservice.event.model.Event;
import org.example.messageservice.event.model.EventState;
import org.example.messageservice.event.model.EventType;
import org.example.messageservice.jooq.tables.records.EventsRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;

import static java.util.Optional.ofNullable;
import static org.example.messageservice.event.model.EventId.eventId;
import static org.example.messageservice.jooq.Tables.EVENTS;

@Repository
public class EventRepository {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public EventRepository(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    public void insert(Event event) {
        dsl.insertInto(EVENTS)
            .set(toRecord(event))
            .execute();
    }

    public void update(Event event) {
        dsl.update(EVENTS)
            .set(toRecord(event))
            .where(EVENTS.ID.eq(event.id().value()))
            .execute();
    }

    public Collection<Event> findAllBy(EventState eventState) {
        return dsl.selectFrom(EVENTS)
            .where(EVENTS.STATE.eq(eventState.name()))
            .fetch()
            .map(this::fromRecord);
    }

    private EventsRecord toRecord(Event event) {
        return new EventsRecord(
            event.id().value(),
            event.state().name(),
            event.type().value(),
            JSON.valueOf(objectMapper.writeValueAsString(event.payload())),
            event.createdDate(),
            event.publishedDate().orElse(null)
        );
    }

    private Event fromRecord(EventsRecord record) {
        return Event.builder()
            .id(eventId(record.getId()))
            .state(EventState.valueOf(record.getState()))
            .type(EventType.eventType(record.getType()))
            .payload(objectMapper.readTree(record.getPayload().data()))
            .createdDate(record.getCreatedDate())
            .publishedDate(ofNullable(record.getPublishedDate()))
            .build();
    }
}
