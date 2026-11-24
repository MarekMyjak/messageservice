package org.example.messageservice.event;

import org.example.messageservice.message.MessageCreatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.example.messageservice.EventTestData.aEvent;
import static org.example.messageservice.event.model.EventState.CREATED;
import static org.example.messageservice.message.model.MessageId.messageId;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherTest {

    private final EventRepository eventRepository = Mockito.mock(EventRepository.class);
    private final KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
    private final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
    private final Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
    private final OutboxPublisher outboxPublisher = new OutboxPublisher(eventRepository, kafkaTemplate, objectMapper, clock);

    @Test
    void publishesEvents() {
        // given
        var messageId = messageId(randomUUID());
        var event = aEvent()
            .payload(new ObjectMapper().createObjectNode().put("messageId", messageId.value().toString()))
            .build();
        when(eventRepository.findAllBy(CREATED)).thenReturn(List.of(event));

        // when
        outboxPublisher.publishEvents();

        // then
        kafkaTemplate.send(
            event.type().value(),
            event.id().value().toString(),
            new MessageCreatedEvent(messageId)
        );
        verify(eventRepository).update(event.publish(clock.instant()));
    }
}