package br.com.fiap.hackaton.producer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.AnswerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AnswerProducer answerProducer;

    @Test
    @DisplayName("Deve enviar para a Routing Key de CONFIRMAÇÃO quando o paciente aceitar")
    void deveEnviarParaFilaConfirmada() {
        // Arrange
        AnswerRequest request = mock(AnswerRequest.class);
        when(request.accepted()).thenReturn(true);

        // Act
        answerProducer.sendAnswer(request);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.ANSWER_EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY_ANSWER_CONFIRMED),
                eq(request)
        );
    }

    @Test
    @DisplayName("Deve enviar para a Routing Key de REJEIÇÃO quando o paciente recusar")
    void deveEnviarParaFilaRejeitada() {
        // Arrange
        AnswerRequest request = mock(AnswerRequest.class);
        when(request.accepted()).thenReturn(false);

        // Act
        answerProducer.sendAnswer(request);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.ANSWER_EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY_ANSWER_REJECTED),
                eq(request)
        );
    }
}