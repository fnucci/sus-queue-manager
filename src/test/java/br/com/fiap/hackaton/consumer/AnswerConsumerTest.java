package br.com.fiap.hackaton.consumer;

import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.exception.handler.RabbitErrorHandler;
import br.com.fiap.hackaton.service.AnswerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerConsumerTest {

    @Mock
    private AnswerService answerService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RabbitErrorHandler rabbitErrorHandler;

    @Mock
    private Channel channel;

    @InjectMocks
    private AnswerConsumer answerConsumer;

    private Message message;
    private MessageProperties messageProperties;
    private AnswerRequest acceptRequest;
    private AnswerRequest rejectRequest;
    private final String jsonBody = "{\"id\": 1}";
    private final long deliveryTag = 123L;

    @BeforeEach
    void setUp() {
        messageProperties = mock(MessageProperties.class);
        message = mock(Message.class);
        acceptRequest = new AnswerRequest("11912345678", Boolean.TRUE); // Supondo que exista o DTO
        rejectRequest = new AnswerRequest("11912345678", Boolean.FALSE); // Supondo que exista o DTO

        lenient().when(message.getBody()).thenReturn(jsonBody.getBytes());
        lenient().when(message.getMessageProperties()).thenReturn(messageProperties);
        lenient().when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);
    }

    @Test
    @DisplayName("Deve confirmar resposta com sucesso e enviar ACK")
    void confirmAnswer_Sucesso() throws Exception {
        // Arrange
        when(objectMapper.readValue(jsonBody, AnswerRequest.class)).thenReturn(acceptRequest);

        // Act
        answerConsumer.confirmAnswer(message, channel);

        // Assert
        verify(answerService).confirmAnswer(acceptRequest);
        verify(channel).basicAck(deliveryTag, false);
        verifyNoInteractions(rabbitErrorHandler);
    }

    @Test
    @DisplayName("Deve rejeitar resposta com sucesso e enviar ACK")
    void rejectAnswer_Sucesso() throws Exception {
        // Arrange
        when(objectMapper.readValue(jsonBody, AnswerRequest.class)).thenReturn(rejectRequest);

        // Act
        answerConsumer.rejectAnswer(message, channel);

        // Assert
        verify(answerService).rejectAnswer(rejectRequest);
        verify(channel).basicAck(deliveryTag, false);
        verifyNoInteractions(rabbitErrorHandler);
    }

    @Test
    @DisplayName("Deve chamar ErrorHandler quando confirmAnswer falhar")
    void confirmAnswer_Erro() throws Exception {
        // Arrange
        Exception exception = new RuntimeException("Erro de parsing");
        when(objectMapper.readValue(anyString(), eq(AnswerRequest.class))).thenThrow(exception);

        // Act
        answerConsumer.confirmAnswer(message, channel);

        // Assert
        verify(rabbitErrorHandler).handleInvalidMessage(message, channel, exception);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(answerService, never()).confirmAnswer(any());
    }

    @Test
    @DisplayName("Deve chamar ErrorHandler quando rejectAnswer falhar")
    void rejectAnswer_Erro() throws Exception {
        // Arrange
        Exception exception = new RuntimeException("Erro no Service");
        when(objectMapper.readValue(jsonBody, AnswerRequest.class)).thenReturn(acceptRequest);
        doThrow(exception).when(answerService).rejectAnswer(acceptRequest);

        // Act
        answerConsumer.rejectAnswer(message, channel);

        // Assert
        verify(rabbitErrorHandler).handleInvalidMessage(message, channel, exception);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}