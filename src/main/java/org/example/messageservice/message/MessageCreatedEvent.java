package org.example.messageservice.message;

import org.example.messageservice.message.model.MessageId;

public record MessageCreatedEvent(
    MessageId messageId
) {
}
