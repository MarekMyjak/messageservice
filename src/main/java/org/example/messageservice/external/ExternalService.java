package org.example.messageservice.external;

import org.example.messageservice.message.model.ExternalId;
import org.example.messageservice.message.model.Message;

public interface ExternalService {

    ExternalId call(Message message);
}
