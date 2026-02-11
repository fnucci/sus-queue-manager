package br.com.fiap.hackaton.consumer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.exception.handler.RabbitErrorHandler;
import br.com.fiap.hackaton.service.AnswerService;
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
public class AnswerConsumer {

    private AnswerService answerService;
    private ObjectMapper objectMapper;
    private RabbitErrorHandler rabbitErrorHandler;


    @RabbitListener(queues = RabbitConfig.ANSWER_QUEUE_CONFIRMED)
    public void confirmAnswer(Message message, Channel channel) {

        String body = new String(message.getBody());

        try {
            AnswerRequest answerRequest = objectMapper.readValue(body, AnswerRequest.class);
            answerService.confirmAnswer(answerRequest);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            rabbitErrorHandler.handleInvalidMessage(message, channel, e);

        }
    }

    @RabbitListener(queues = RabbitConfig.ANSWER_QUEUE_REJECTED)
    public void rejectAnswer(Message message, Channel channel) {

        String body = new String(message.getBody());

        try {
            AnswerRequest answerRequest = objectMapper.readValue(body, AnswerRequest.class);
            answerService.rejectAnswer(answerRequest);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            rabbitErrorHandler.handleInvalidMessage(message, channel, e);
        }
    }
}
