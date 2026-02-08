package br.com.fiap.hackaton.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@Slf4j
public class RabbitConfig {

    public static final String INTEREST_EXCHANGE = "interest_exchange";
    public static final String INTEREST_QUEUE = "interest_queue";
    public static final String ROUTING_KEY_INTEREST = "interest.new";

    public static final String AVAILABILITY_EXCHANGE = "availability_exchange";
    public static final String AVAILABILITY_QUEUE = "availability_queue";
    public static final String ROUTING_KEY_AVAILABILITY = "availability.new";

    @Bean
    public DirectExchange interestExchange() {
        return new DirectExchange(INTEREST_EXCHANGE);
    }

    @Bean
    public Queue interestQueue() {
        return new Queue(INTEREST_QUEUE, true);
    }

    @Bean
    public Binding interestBinding() {
        return BindingBuilder.bind(interestQueue()).to(interestExchange()).with(ROUTING_KEY_INTEREST);
    }

    @Bean
    public DirectExchange availabilityExchange() {
        return new DirectExchange(AVAILABILITY_EXCHANGE);
    }

    @Bean
    public Queue availabilityQueue() {
        return new Queue(AVAILABILITY_QUEUE, true);
    }

    @Bean
    public Binding availabilityBinding() {
        return BindingBuilder.bind(availabilityQueue()).to(availabilityExchange()).with(ROUTING_KEY_AVAILABILITY);
    }

    @Bean
    public Jackson2JsonMessageConverter producerMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setIdClassMapping(Collections.emptyMap());
        typeMapper.setTrustedPackages("br.com.fiap.hackaton.dto.request", "java.util", "java.lang");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter producerMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(producerMessageConverter);
        return template;
    }
}
