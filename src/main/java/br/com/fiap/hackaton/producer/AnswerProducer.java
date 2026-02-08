package br.com.fiap.hackaton.producer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.AnswerRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AnswerProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendAnswer(@Valid AnswerRequest request) {
        log.info("Verificando a resposta do paciente para marcação.");
        String routingKey = request.accepted() ? RabbitConfig.ROUTING_KEY_ANSWER_CONFIRMED : RabbitConfig.ROUTING_KEY_ANSWER_REJECTED;
        log.info("Enviando disponibilidade para RabbitMQ: {}", request);
        rabbitTemplate.convertAndSend(
                RabbitConfig.ANSWER_EXCHANGE,
                routingKey,
                request);

    }
}
