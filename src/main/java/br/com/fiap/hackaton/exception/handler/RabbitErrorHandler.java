package br.com.fiap.hackaton.exception.handler;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
public class RabbitErrorHandler implements ErrorHandler {


    @Override
    public void handleError(Throwable t) {
        log.error("Erro ao processar mensagem RabbitMQ", t);
    }

    public void handleInvalidMessage(Message message, Channel channel, Exception e) {
        log.warn("Mensagem inválida descartada: {} - Motivo: {}", message.getBody(), e.getMessage());
        try {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            log.error("Erro ao descartar mensagem do RabbitMQ", ex);
        }
    }
}
