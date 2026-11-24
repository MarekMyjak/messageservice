package org.example.messageservice.message.model.deserializer;

import org.example.messageservice.message.model.MessageId;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.UUID;

@JacksonComponent
public class MessageIdDeserializer extends ValueDeserializer<MessageId> {

    @Override
    public MessageId deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        return new MessageId(UUID.fromString(parser.getString()));
    }
}
