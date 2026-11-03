package com.ruvi.reconciliation.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMqConfig {

    @Value("${reconciliation.rabbit.payments-exchange}")
    private String exchange;

    @Value("${reconciliation.rabbit.payments-queue}")
    private String queue;

    @Value("${reconciliation.rabbit.dlx}")
    private String dlx;

    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public TopicExchange paymentsDlx() {
        return new TopicExchange(dlx, true, false);
    }

    @Bean
    public Queue paymentsQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", dlx)
                .build();
    }

    @Bean
    public Queue paymentsDlqQueue() {
        return QueueBuilder.durable(queue + ".dlq").build();
    }

    @Bean
    public Binding paymentsBinding(Queue paymentsQueue, TopicExchange paymentsExchange) {
        return BindingBuilder.bind(paymentsQueue).to(paymentsExchange).with("payment.#");
    }

    @Bean
    public Binding paymentsDlqBinding(Queue paymentsDlqQueue, TopicExchange paymentsDlx) {
        return BindingBuilder.bind(paymentsDlqQueue).to(paymentsDlx).with("#");
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(converter);
        t.setMandatory(true);
        return t;
    }
}
