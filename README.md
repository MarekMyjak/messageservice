# Message Service

A Spring Boot application for submitting and processing messages asynchronously.

The service stores incoming messages in PostgreSQL and publishes corresponding events to Kafka. It uses jOOQ for database access and Spring transactions to ensure data consistency.

## Architecture

The application consists of three main parts:

- **Message API** – accepts incoming message requests and responds immediately after the message and its event have been saved. It does not wait for Kafka or the external service to process the message.
- **Repositories** – persist messages and events in PostgreSQL using jOOQ.
- **Outbox Publisher** – reads unpublished events from the database and publishes them to Kafka for asynchronous processing.

## Transactional Outbox Pattern

The service uses the Transactional Outbox Pattern to reliably publish events without requiring a distributed transaction between PostgreSQL and Kafka.

When a message is submitted:

1. A new message is created.
2. A corresponding event is created.
3. Both records are saved in the same database transaction.
4. The REST API responds successfully without waiting for event delivery or external service processing.
5. A background publisher reads unpublished events from the outbox table. 
6. Events are published to Kafka. 
7. After successful publication, the event is marked as published.
8. A Kafka consumer processes the event and communicates with the external service.

Because the message and its event are stored in a single database transaction, either both records are committed or neither is. This prevents a message from being saved without its corresponding event.

### Stability and Reliability

- This architecture improves the stability and responsiveness of the API:
- Fast responses – requests are not blocked by Kafka publishing or external service response times.
- Resilience to external failures – temporary Kafka or external service outages do not prevent the API from accepting and storing messages.
- Persistent delivery – events remain in the database until successfully published, allowing processing to resume after failures or application restarts.
- Decoupled processing – message submission and external service communication happen independently.

### Delivery Guarantees

Publishing to Kafka and updating the database cannot be performed atomically with a regular database transaction. Therefore, an event may occasionally be published more than once—for example, if the application crashes after Kafka accepts the event but before the database is updated.

Consumers should be idempotent and handle duplicate events safely, 

## Technology Stack

- Java
- Spring Boot
- PostgreSQL
- jOOQ
- Flyway
- Apache Kafka

## Testing

Integration tests use embedded PostgreSQL and Kafka instances, allowing the application to be tested without external infrastructure or Docker.

You can run tests in IDE or in terminal using this command:
```
./gradlew test
```