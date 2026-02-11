package br.com.fiap.hackaton.producer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.AddressRequest;
import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AvailabilityProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AvailabilityProducer availabilityProducer;

    @Test
    @DisplayName("Deve enviar a disponibilidade para o RabbitMQ com os parâmetros corretos")
    void deveEnviarDisponibilidadeParaRabbitMQ() {
        // Arrange
        AvailabilityRequest request = new AvailabilityRequest("TestePrestador", new AddressRequest("Rua teste", "103", "Vila da telha", "Sao Paulo", "SP", "04833101"), "Tomografia Computadorizada", "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911", OffsetDateTime.now()); // Presumindo construtor padrão
        // Configure as propriedades do request se necessário, ex:
        // request.setExamHashCode("HASH-TESTE");

        // Act
        availabilityProducer.sendAvailability(request);

        // Assert
        // Verificamos se o convertAndSend foi chamado exatamente uma vez com os parâmetros esperados
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitConfig.AVAILABILITY_EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY_AVAILABILITY),
                eq(request)
        );
    }
}