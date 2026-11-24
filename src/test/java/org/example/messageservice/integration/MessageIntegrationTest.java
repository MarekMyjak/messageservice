package org.example.messageservice.integration;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.example.messageservice.external.ExternalService;
import org.example.messageservice.message.MessageRepository;
import org.example.messageservice.message.model.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED;
import static java.time.Duration.ofSeconds;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.example.messageservice.message.model.Content.content;
import static org.example.messageservice.message.model.ExternalId.externalId;
import static org.example.messageservice.message.model.MessageState.SENT;
import static org.example.messageservice.message.model.RequestId.requestId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureEmbeddedDatabase(provider = EMBEDDED)
@AutoConfigureRestTestClient
@EmbeddedKafka
public class MessageIntegrationTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ExternalService externalService;

    @Test
    void storeMessageAndSentToExternalService() {
        // given
        var requestId = requestId(randomUUID());
        var externalId = externalId(randomUUID());
        when(externalService.call(any(Message.class))).thenReturn(externalId);

        // when
        restTestClient.post()
            .uri("/messages")
            .contentType(APPLICATION_JSON)
            .body(String.format("""
                {
                    "requestId": "%s",
                    "content": "Hello"
                }
                """, requestId.value()))
            .exchange()
            .expectStatus().isCreated();

        // then
        await()
            .atMost(ofSeconds(5))
            .untilAsserted(() -> {
                var message = messageRepository.get(requestId);

                assertThat(message.content()).isEqualTo(content("Hello"));
                assertThat(message.requestId()).isEqualTo(requestId);
                assertThat(message.state()).isEqualTo(SENT);
                assertThat(message.externalId()).hasValue(externalId);
            });
    }

    @Test
    void completeMultipleRequestWithUnstableExternalService() {
        // given
        var externalId = externalId(randomUUID());
        var attempts = new AtomicInteger();
        var requestIds = IntStream.range(0, 100)
            .mapToObj(_ -> requestId(randomUUID()))
            .toList();
        when(externalService.call(any(Message.class)))
            .thenAnswer(_ -> {
                if (attempts.incrementAndGet() % 2 == 0) {
                    throw new IllegalStateException("Simulated failure");
                }

                return externalId;
            });

        // when
        for (int i = 0; i < 100; i++) {
            restTestClient.post()
                .uri("/messages")
                .contentType(APPLICATION_JSON)
                .body(String.format("""
                    {
                        "requestId": "%s",
                        "content": "Hello %s"
                    }
                    """, requestIds.get(i).value(), i))
                .exchange()
                .expectStatus().isCreated();
        }

        // then
        await()
            .atMost(ofSeconds(60))
            .untilAsserted(() -> {
                var messages = messageRepository.findAllBy(requestIds);
                assertThat(messages.size()).isEqualTo(100);

                assertThat(messages)
                    .extracting(Message::state)
                    .containsOnly(SENT);

                verify(externalService, Mockito.atLeast(150)).call(any(Message.class));
            });
    }

    @Test
    void storeMessageOnceWhenTwoRequestShareTheSameRequestId() {
        // given
        var requestId = requestId(randomUUID());
        var externalId = externalId(randomUUID());
        when(externalService.call(any(Message.class))).thenReturn(externalId);

        restTestClient.post()
            .uri("/messages")
            .contentType(APPLICATION_JSON)
            .body(String.format("""
                {
                    "requestId": "%s",
                    "content": "Hello"
                }
                """, requestId.value()))
            .exchange()
            .expectStatus().isCreated();

        // when second request with the same requestId
        restTestClient.post()
            .uri("/messages")
            .contentType(APPLICATION_JSON)
            .body(String.format("""
                {
                    "requestId": "%s",
                    "content": "Hello"
                }
                """, requestId.value()))
            .exchange()
            .expectStatus().isCreated();

        // then
        await()
            .atMost(ofSeconds(5))
            .untilAsserted(() -> {
                var message = messageRepository.get(requestId);

                assertThat(message.content()).isEqualTo(content("Hello"));
                assertThat(message.requestId()).isEqualTo(requestId);
                assertThat(message.state()).isEqualTo(SENT);
                assertThat(message.externalId()).hasValue(externalId);

                verify(externalService, times(1)).call(any(Message.class));
            });
    }
}