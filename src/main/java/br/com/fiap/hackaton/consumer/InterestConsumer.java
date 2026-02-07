package br.com.fiap.hackaton.consumer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.handler.RabbitErrorHandler;
import br.com.fiap.hackaton.service.InterestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class InterestConsumer {

    private InterestService interestService;

    private ObjectMapper objectMapper;

    private RabbitErrorHandler rabbitErrorHandler;

    @RabbitListener(queues = RabbitConfig.INTEREST_QUEUE)
    public void recieveInterest(Message message, Channel channel) {

        String body = new String(message.getBody());

        try {
            InterestRequest interestRequest = objectMapper.readValue(body, InterestRequest.class);
            interestService.registerInterest(interestRequest);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            rabbitErrorHandler.handleInvalidMessage(message, channel, e);

        }
    }
}
