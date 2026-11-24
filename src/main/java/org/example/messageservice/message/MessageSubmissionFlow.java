package org.example.messageservice.message;

import org.example.messageservice.event.EventRepository;
import org.example.messageservice.event.model.Event;
import org.example.messageservice.message.model.Content;
import org.example.messageservice.message.model.Message;
import org.example.messageservice.message.model.RequestId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;

import static org.example.messageservice.event.model.EventType.MESSAGE_CREATED_EVENT;

@Service
public class MessageSubmissionFlow {

    private final MessageRepository messageRepository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public MessageSubmissionFlow(MessageRepository messageRepository, EventRepository eventRepository, ObjectMapper objectMapper, Clock clock) {
        this.messageRepository = messageRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public void submit(RequestId requestId, Content content) {
        if (messageRepository.existBy(requestId)) {
            return;
        }

        var message = Message.builder()
            .requestId(requestId)
            .content(content)
            .build();

        var event = Event.builder()
            .payload(objectMapper.createObjectNode()
                .put("messageId", message.id().value().toString()))
            .type(MESSAGE_CREATED_EVENT)
            .createdDate(clock.instant())
            .build();

        messageRepository.insert(message);
        eventRepository.insert(event);
    }
}
