package org.example.messageservice.message.model;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public record MessageId(UUID value) {

    public MessageId {
        checkNotNull(value, "messageId");
    }

    public static MessageId messageId(UUID value) {
        return new MessageId(value);
    }
}
