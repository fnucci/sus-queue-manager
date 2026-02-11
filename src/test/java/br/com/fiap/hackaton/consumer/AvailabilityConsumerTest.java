package br.com.fiap.hackaton.consumer;

import br.com.fiap.hackaton.dto.request.AddressRequest;
import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import br.com.fiap.hackaton.exception.handler.RabbitErrorHandler;
import br.com.fiap.hackaton.service.AvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityConsumerTest {

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RabbitErrorHandler rabbitErrorHandler;

    @Mock
    private Channel channel;

    @InjectMocks
    private AvailabilityConsumer availabilityConsumer;

    @Test
    @DisplayName("Deve processar mensagem com sucesso e confirmar o recebimento (ACK)")
    void deveProcessarMensagemComSucesso() throws Exception {
        // Arrange
        String jsonBody = "{\"id\": 123}";
        long deliveryTag = 1L;

        Message message = mock(Message.class);
        MessageProperties props = mock(MessageProperties.class);

        AvailabilityRequest request = new AvailabilityRequest("TestePrestador", new AddressRequest("Rua teste", "103", "Vila da telha", "Sao Paulo", "SP", "04833101"), "Tomografia Computadorizada", "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911", OffsetDateTime.now()); // Presumindo construtor padrão

        when(message.getBody()).thenReturn(jsonBody.getBytes());
        when(message.getMessageProperties()).thenReturn(props);
        when(props.getDeliveryTag()).thenReturn(deliveryTag);
        when(objectMapper.readValue(jsonBody, AvailabilityRequest.class)).thenReturn(request);

        // Act
        availabilityConsumer.recieveAvailability(message, channel);

        // Assert
        verify(availabilityService).registerAvailability(request);
        verify(channel).basicAck(deliveryTag, false);
        verifyNoInteractions(rabbitErrorHandler);
    }

    @Test
    @DisplayName("Deve acionar o RabbitErrorHandler quando ocorrer erro no processamento")
    void deveAcionarErrorHandlerEmCasoDeExcecao() throws Exception {
        // Arrange
        String invalidJson = "invalid-json";
        Message message = mock(Message.class);
        Exception exception = new RuntimeException("Erro ao processar");

        when(message.getBody()).thenReturn(invalidJson.getBytes());
        // Simulando erro na desserialização
        when(objectMapper.readValue(any(String.class), eq(AvailabilityRequest.class)))
                .thenThrow(exception);

        // Act
        availabilityConsumer.recieveAvailability(message, channel);

        // Assert
        verify(rabbitErrorHandler).handleInvalidMessage(message, channel, exception);
        verify(availabilityService, never()).registerAvailability(any());
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}