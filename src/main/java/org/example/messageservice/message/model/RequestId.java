package org.example.messageservice.message.model;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public record RequestId(UUID value) {

    public RequestId {
        checkNotNull(value, "requestId");
    }

    public static RequestId requestId(UUID value) {
        return new RequestId(value);
    }
}
