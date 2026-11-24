package org.example.messageservice.event;

import org.example.messageservice.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;

import static org.example.messageservice.event.model.EventState.CREATED;

@Component
class OutboxPublisher {

    private final EventRepository eventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public OutboxPublisher(EventRepository eventRepository, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper, Clock clock) {
        this.eventRepository = eventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 200)
    void publishEvents() {
        eventRepository.findAllBy(CREATED)
            .forEach(this::publish);
    }

    private void publish(Event event) {
        try {
            var eventClass = Class.forName(event.type().value());

            var payload = objectMapper.treeToValue(
                event.payload(),
                eventClass
            );

            kafkaTemplate.send(
                event.type().value(),
                event.id().value().toString(),
                payload
            ).get();
            eventRepository.update(event.publish(clock.instant()));

        } catch (Exception e) {
            System.out.println("Error publishing event: " + e);
        }
    }
}