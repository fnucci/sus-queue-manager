package br.com.fiap.hackaton.consumer;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.handler.RabbitErrorHandler;
import br.com.fiap.hackaton.service.InterestService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestConsumerTest {

    @Mock
    private InterestService interestService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RabbitErrorHandler rabbitErrorHandler;

    @Mock
    private Channel channel;

    @InjectMocks
    private InterestConsumer interestConsumer;

    @Test
    @DisplayName("Deve processar mensagem e confirmar (ACK) com sucesso")
    void deveProcessarMensagemComSucesso() throws Exception {
        // Arrange
        String json = """
                {
                        "pacienteName": "teste",
                        "pacienteCns": "2139847582914",
                        "phoneNumber": "11981048890",
                        "examName": "Tomografia computadorizada",
                        "examHashCode": "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911"
                }""";
        Message message = new Message(json.getBytes(), createProperties(1L));
        InterestRequest request = new InterestRequest("Teste", "2139847582914", "11981048890", "Tomografia computadorizada", "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911");

        when(objectMapper.readValue(json, InterestRequest.class)).thenReturn(request);

        // Act
        interestConsumer.recieveInterest(message, channel);

        // Assert
        verify(interestService).registerInterest(request);
        verify(channel).basicAck(1L, false);
        verifyNoInteractions(rabbitErrorHandler);
    }

    @Test
    @DisplayName("Deve chamar o RabbitErrorHandler quando ocorrer erro na desserialização ou processamento")
    void deveChamarErrorHandlerEmCasoDeErro() throws Exception {
        // Arrange
        String json = "invalid-json";
        Message message = new Message(json.getBytes(), createProperties(2L));
        RuntimeException exception = new RuntimeException("Erro de parsing");

        when(objectMapper.readValue(anyString(), eq(InterestRequest.class))).thenThrow(exception);

        // Act
        interestConsumer.recieveInterest(message, channel);

        // Assert
        // Verifica se o erro foi repassado para o seu handler customizado
        verify(rabbitErrorHandler).handleInvalidMessage(eq(message), eq(channel), eq(exception));
        // Garante que o ack NÃO foi chamado
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    private MessageProperties createProperties(Long deliveryTag) {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(deliveryTag);
        return properties;
    }
}
