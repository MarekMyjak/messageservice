package org.example.messageservice.event.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.example.messageservice.EventTestData.aEvent;
import static org.example.messageservice.event.model.EventState.PUBLISHED;

class EventTest {

    @Test
    void publishEvent() {
        // given
        var event = aEvent().build();
        var publishedDate = Instant.now();

        // when
        var result = event.publish(publishedDate);

        // then
        assertThat(result.state()).isEqualTo(PUBLISHED);
        assertThat(result.publishedDate()).isEqualTo(Optional.of(publishedDate));
    }

    @Test
    void throwsWhenNullIdValues() {
        assertThatThrownBy(() -> aEvent().id(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("id");
    }

    @Test
    void throwsWhenNullStateValues() {
        assertThatThrownBy(() -> aEvent().state(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("state");
    }

    @Test
    void throwsWhenNullTypeIdValues() {
        assertThatThrownBy(() -> aEvent().type(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("type");
    }

    @Test
    void throwsWhenNullPayloadValues() {
        assertThatThrownBy(() -> aEvent().payload(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("payload");
    }

    @Test
    void throwsWhenNullCreatedDateValues() {
        assertThatThrownBy(() -> aEvent().createdDate(null).build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("createdDate");
    }

    @Test
    void throwsWhenMessageInPublishedStateDoesNotHavePublishedDate() {
        assertThatThrownBy(() -> aEvent().state(PUBLISHED).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PublishedDate is required in PUBLISHED state");
    }
}