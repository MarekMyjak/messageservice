package org.example.messageservice.message;

import org.example.messageservice.external.ExternalService;
import org.example.messageservice.message.model.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.example.messageservice.MessageTestData.aMessage;
import static org.example.messageservice.message.model.ExternalId.externalId;
import static org.example.messageservice.message.model.MessageState.SENT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageCreatedConsumerTest {

    private final MessageRepository messageRepository = Mockito.mock(MessageRepository.class);
    private final ExternalService externalService = Mockito.mock(ExternalService.class);
    private final MessageCreatedConsumer consumer = new MessageCreatedConsumer(messageRepository, externalService);

    @Test
    void sendsMessage() {
        // given
        var message = aMessage().build();
        var externalId = externalId(randomUUID());
        when(messageRepository.get(message.id())).thenReturn(message);
        when(externalService.call(message)).thenReturn(externalId);

        // when
        consumer.consume(new MessageCreatedEvent(message.id()));

        // then
        verify(messageRepository).update(new Message(
            message.id(),
            message.requestId(),
            SENT,
            message.content(),
            Optional.of(externalId)
        ));
    }
}