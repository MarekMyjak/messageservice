package org.example.messageservice.message;

import org.example.messageservice.external.ExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MessageCreatedConsumer {

    private final MessageRepository messageRepository;
    private final ExternalService externalService;

    @Autowired
    public MessageCreatedConsumer(MessageRepository messageRepository, ExternalService externalService) {
        this.messageRepository = messageRepository;
        this.externalService = externalService;
    }

    @KafkaListener(
        topics = "org.example.messageservice.message.MessageCreatedEvent",
        groupId = "default"
    )
    @RetryableTopic(
        attempts = "${app.kafka.retry.attempts}",
        backOff = @BackOff(delayString = "${app.kafka.retry.delay}")
    )
    @Transactional
    public void consume(MessageCreatedEvent event) {
        var message = messageRepository.get(event.messageId());
        var externalId = externalService.call(message);
        messageRepository.update(message.send(externalId));
    }
}

