package org.example.messageservice.message;

import org.example.messageservice.message.model.RequestId;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.example.messageservice.message.model.Content.content;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageControllerTest {
    private final MessageSubmissionFlow messageSubmissionFlow = mock(MessageSubmissionFlow.class);
    private final MessageController messageController = new MessageController(messageSubmissionFlow);

    @Test
    void submitsMessage() {
        // given
        var requestId = RequestId.requestId(randomUUID());
        var request = new ObjectMapper().readTree(
            format("""
                    {
                        "requestId": "%s",
                        "content": "Hello"
                    }
                """, requestId.value())
        );

        // when
        messageController.submitMessage(request);

        // then
        verify(messageSubmissionFlow).submit(requestId, content("Hello"));
    }
}