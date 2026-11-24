package org.example.messageservice.message;

import org.example.messageservice.jooq.tables.records.MessagesRecord;
import org.example.messageservice.message.model.ExternalId;
import org.example.messageservice.message.model.Message;
import org.example.messageservice.message.model.MessageId;
import org.example.messageservice.message.model.MessageState;
import org.example.messageservice.message.model.RequestId;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.example.messageservice.jooq.Tables.MESSAGES;
import static org.example.messageservice.message.model.Content.content;
import static org.example.messageservice.message.model.MessageId.messageId;
import static org.example.messageservice.message.model.RequestId.requestId;

@Repository
public class MessageRepository {

    private final DSLContext dsl;

    public MessageRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(Message message) {
        dsl.insertInto(MESSAGES)
            .set(toRecord(message))
            .execute();
    }

    public void update(Message message) {
        dsl.update(MESSAGES)
            .set(toRecord(message))
            .where(MESSAGES.ID.eq(message.id().value()))
            .execute();
    }

    public Message get(MessageId messageId) {
        return dsl.selectFrom(MESSAGES)
            .where(MESSAGES.ID.eq(messageId.value()))
            .fetchOptional()
            .map(MessageRepository::fromRecord)
            .orElseThrow(() -> new IllegalStateException(String.format("Message by id %s not found.", messageId)));
    }


    public Message get(RequestId requestId) {
        return dsl.selectFrom(MESSAGES)
            .where(MESSAGES.REQUEST_ID.eq(requestId.value()))
            .fetchOptional()
            .map(MessageRepository::fromRecord)
            .orElseThrow(() -> new IllegalStateException(String.format("Message by requestId %s not found.", requestId)));
    }

    public Collection<Message> findAllBy(List<RequestId> requestIds) {
        return dsl.selectFrom(MESSAGES)
            .where(MESSAGES.REQUEST_ID.in(requestIds.stream().map(RequestId::value).toList()))
            .fetch()
            .map(MessageRepository::fromRecord);
    }

    public Boolean existBy(RequestId requestId) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(MESSAGES)
                .where(MESSAGES.REQUEST_ID.eq(requestId.value()))
        );
    }

    private static MessagesRecord toRecord(Message message) {
        return new MessagesRecord(
            message.id().value(),
            message.requestId().value(),
            message.state().name(),
            message.content().value(),
            message.externalId().map(ExternalId::value).orElse(null)
        );
    }

    private static Message fromRecord(MessagesRecord record) {
        return new Message(
            messageId(record.getId()),
            requestId(record.getRequestId()),
            MessageState.valueOf(record.getState()),
            content(record.getContent()),
            ofNullable(record.getExternalId()).map(ExternalId::externalId)
        );
    }
}
