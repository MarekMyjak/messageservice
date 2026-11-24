package org.example.messageservice.message.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record Content(String value) {

    public Content {
        checkNotNull(value, "externalId");
        checkArgument(!value.isBlank(), "Content cannot be blank.");
        checkArgument(value.length() <= 255, "Content maximum length is 255 characters.");
    }

    public static Content content(String value) {
        return new Content(value);
    }
}

