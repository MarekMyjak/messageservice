package org.example.messageservice.integration;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.example.messageservice.external.ExternalService;
import org.example.messageservice.message.MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.messageservice.MessageTestData.aMessage;

@SpringBootTest
@AutoConfigureEmbeddedDatabase(provider = EMBEDDED)
public class MessageRepositoryIntegrationTest {

    @Autowired
    private MessageRepository messageRepository;

    @MockitoBean
    private ExternalService externalService;

    @Test
    void rejectsMessageWithDuplicateRequestId() {
        // given
        var message = aMessage().build();
        messageRepository.insert(message);

        // when / then
        assertThatThrownBy(() ->
            messageRepository.insert(aMessage().requestId(message.requestId()).build()))
            .isInstanceOf(DuplicateKeyException.class);
    }
}