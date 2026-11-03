package com.ruvi.reconciliation.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher {

    private final RabbitTemplate rabbit;
    private final ObjectMapper objectMapper;
    private final String exchange;

    public RabbitEventPublisher(RabbitTemplate rabbit, ObjectMapper objectMapper,
                                @Value("${reconciliation.rabbit.payments-exchange}") String exchange) {
        this.rabbit = rabbit;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
    }

    public void publish(String routingKey, String eventType, String aggregateId, String payloadJson) {
        try {
            JsonNode node = objectMapper.readTree(payloadJson);
            Message msg = MessageBuilder.withBody(objectMapper.writeValueAsBytes(node))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setHeader("eventType", eventType)
                    .setHeader("aggregateId", aggregateId)
                    .setDeliveryMode(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbit.send(exchange, routingKey, msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event " + eventType, e);
        }
    }
}
