package org.example.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "payment.exchange";
    public static final String QUEUE = "payment.purchase.queue";
    public static final String ROUTING_KEY = "shaparak.purchase";

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConvertor(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setReplyTimeout(10_000);
        return template;
    }
}
