package org.example.messageservice.message;

import org.example.messageservice.event.EventRepository;
import org.example.messageservice.message.model.Message;
import org.example.messageservice.message.model.RequestId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static java.time.Clock.fixed;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.messageservice.MessageTestData.aMessage;
import static org.example.messageservice.event.model.EventType.MESSAGE_CREATED_EVENT;
import static org.example.messageservice.message.model.Content.content;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MessageSubmissionFlowTest {

    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Clock clock = fixed(Instant.now(), ZoneOffset.UTC);
    private final MessageSubmissionFlow messageSubmissionFlow = new MessageSubmissionFlow(messageRepository, eventRepository, objectMapper, clock);

    @Test
    void storesMessageWithEvent() {
        // given
        var requestId = RequestId.requestId(randomUUID());
        var content = content("Hello");
        when(messageRepository.existBy(requestId)).thenReturn(false);

        // when
        messageSubmissionFlow.submit(requestId, content);

        // then
        var messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).insert(messageCaptor.capture());

        var message = messageCaptor.getValue();
        assertThat(message.content()).isEqualTo(content);
        assertThat(message.requestId()).isEqualTo(requestId);

        verify(eventRepository).insert(argThat(event -> {
            assertThat(event.type()).isEqualTo(MESSAGE_CREATED_EVENT);
            assertThat(event.payload()).isEqualTo(objectMapper.createObjectNode().put("messageId", message.id().value().toString()));
            assertThat(event.createdDate()).isEqualTo(clock.instant());
            return true;
        }));
    }

    @Test
    void doesNothingIfMessageWasAlreadyStored() {
        // given
        var message = aMessage().build();
        var content = content("Hello");
        when(messageRepository.existBy(message.requestId())).thenReturn(true);

        // when
        messageSubmissionFlow.submit(message.requestId(), content);

        // then
        verify(messageRepository).existBy(message.requestId());
        verifyNoMoreInteractions(messageRepository);
        verifyNoInteractions(eventRepository);
    }
}