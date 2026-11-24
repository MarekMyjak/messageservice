package org.example.messageservice.message.model;


import java.util.Optional;

import static com.google.common.base.Preconditions.*;
import static java.util.UUID.randomUUID;
import static org.example.messageservice.message.model.MessageId.messageId;
import static org.example.messageservice.message.model.MessageState.CREATED;
import static org.example.messageservice.message.model.MessageState.SENT;

public record Message(
    MessageId id,
    RequestId requestId,
    MessageState state,
    Content content,
    Optional<ExternalId> externalId
) {
    public Message {
        checkNotNull(id, "id");
        checkNotNull(requestId, "requestId");
        checkNotNull(state, "state");
        checkNotNull(content, "content");
        checkNotNull(externalId, "externalId");

        if (state == SENT) {
            checkArgument(externalId.isPresent(), "ExternalId is required in SENT state");
        }
    }

    public Message send(ExternalId externalId) {
        checkState(state == CREATED, "Expected state %s but was %s", CREATED, state);
        return copy()
            .state(SENT)
            .externalId(Optional.of(externalId))
            .build();
    }

    private Builder copy() {
        return builder()
            .id(id)
            .requestId(requestId)
            .state(state)
            .content(content)
            .externalId(externalId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MessageId id = messageId(randomUUID());
        private RequestId requestId;
        private MessageState state = CREATED;
        private Content content;
        private Optional<ExternalId> externalId = Optional.empty();

        public Builder id(MessageId id) {
            this.id = id;
            return this;
        }

        public Builder requestId(RequestId requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder state(MessageState state) {
            this.state = state;
            return this;
        }

        public Builder content(Content content) {
            this.content = content;
            return this;
        }

        public Builder externalId(Optional<ExternalId> externalId) {
            this.externalId = externalId;
            return this;
        }

        public Message build() {
            return new Message(
                id,
                requestId,
                state,
                content,
                externalId
            );
        }
    }
}
