package org.example.messageservice.message;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import static java.util.UUID.fromString;
import static org.example.messageservice.message.model.Content.content;
import static org.example.messageservice.message.model.RequestId.requestId;

@RestController
public class MessageController {

    private final MessageSubmissionFlow messageSubmissionFlow;

    public MessageController(MessageSubmissionFlow messageSubmissionFlow) {
        this.messageSubmissionFlow = messageSubmissionFlow;
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void submitMessage(@RequestBody JsonNode body) {
        var requestId = requestId(fromString(body.get("requestId").asString()));
        var content = content(body.get("content").asString());

        messageSubmissionFlow.submit(requestId, content);
    }
}
