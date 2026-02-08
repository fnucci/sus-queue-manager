package br.com.fiap.hackaton.producer;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.dto.request.InterestRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InterestProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private InterestProducer interestProducer;

    @Test
    @DisplayName("Deve chamar o convertAndSend com os parâmetros corretos da configuração")
    void deveEnviarMensagemParaORabbitComSucesso() {
        // Arrange (Preparação)
        InterestRequest request = new InterestRequest("Teste", "1234567890", "11981048435", "Tomografia teste", "ausdjhyrfgawijdfbuhwgfg83724194g");

        // Act (Ação)
        interestProducer.sendInterest(request);

        // Assert (Verificação)
        // Verificamos se o rabbitTemplate foi chamado exatamente 1 vez
        // com a Exchange e Routing Key definidas na sua RabbitConfig
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitConfig.INTEREST_EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY_INTEREST),
                eq(request)
        );
    }
}
