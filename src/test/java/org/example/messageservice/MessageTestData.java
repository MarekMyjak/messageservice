package org.example.messageservice;

import org.example.messageservice.message.model.Message;
import org.example.messageservice.message.model.MessageState;

import static java.util.UUID.randomUUID;
import static org.example.messageservice.message.model.Content.content;
import static org.example.messageservice.message.model.MessageId.messageId;
import static org.example.messageservice.message.model.RequestId.requestId;

public interface MessageTestData {

    static Message.Builder aMessage() {
        return Message.builder()
            .id(messageId(randomUUID()))
            .requestId(requestId(randomUUID()))
            .content(content("Test message"))
            .state(MessageState.CREATED);
    }
}
