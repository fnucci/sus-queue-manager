package br.com.fiap.hackaton.producer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AvailabilityProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendAvailability(AvailabilityRequest request) {

        log.info("Enviando interesse para RabbitMQ: {}", request);
        rabbitTemplate.convertAndSend(
                RabbitConfig.AVAILABILITY_EXCHANGE,
                RabbitConfig.ROUTING_KEY_AVAILABILITY,
                request);
    }
}
