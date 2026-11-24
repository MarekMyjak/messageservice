package org.example.messageservice.message.model;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public record ExternalId(UUID value) {

    public ExternalId {
        checkNotNull(value, "externalId");
    }

    public static ExternalId externalId(UUID value) {
        return new ExternalId(value);
    }
}
