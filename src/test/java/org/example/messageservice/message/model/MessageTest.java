package org.example.messageservice.message.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.example.messageservice.MessageTestData.aMessage;
import static org.example.messageservice.message.model.ExternalId.externalId;
import static org.example.messageservice.message.model.MessageState.SENT;

class MessageTest {

    @Test
    void sendsMessage() {
        // given
        var message = aMessage().build();
        var externalId = externalId(randomUUID());

        // when
        var result = message.send(externalId);

        // then
        assertThat(result.state()).isEqualTo(SENT);
        assertThat(result.externalId()).isEqualTo(Optional.of(externalId));
    }

    @Test
    void doNothingWhenItsSent() {
        // given
        var externalId = externalId(randomUUID());
        var message = aMessage().state(SENT).externalId(Optional.of(externalId)).build();

        // when
        var result = message.send(externalId);

        // then
        assertThat(result).isEqualTo(message);
    }

    @Test
    void throwsWhenExternalIdIsDifferent() {
        var message = aMessage().state(SENT).externalId(Optional.of(externalId(randomUUID()))).build();

        assertThatThrownBy(() -> message.send(externalId(randomUUID())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Argument publishedDate have to be the same as existing publishedDate");
    }

    @Test
    void throwsWhenNullIdValues() {
        assertThatThrownBy(() -> aMessage().id(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("id");
    }

    @Test
    void throwsWhenNullRequestIdValues() {
        assertThatThrownBy(() -> aMessage().requestId(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("requestId");
    }

    @Test
    void throwsWhenNullStateValues() {
        assertThatThrownBy(() -> aMessage().state(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("state");
    }

    @Test
    void throwsWhenNullContentValues() {
        assertThatThrownBy(() -> aMessage().content(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("content");
    }

    @Test
    void throwsWhenNullExternalIdValues() {
        assertThatThrownBy(() -> aMessage().externalId(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("externalId");
    }

    @Test
    void throwsWhenMessageInSentStateDoesNotHaveExternalId() {
        assertThatThrownBy(() -> aMessage().state(SENT).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ExternalId is required in SENT state");
    }
}