package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.producer.AnswerProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WhatsAppWebhookControllerTest {

    @Mock
    private AnswerProducer answerProducer;

    @InjectMocks
    private WhatsAppWebhookController whatsAppWebhookController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Configuração Standalone (sem carregar o contexto completo do Spring)
        mockMvc = MockMvcBuilders.standaloneSetup(whatsAppWebhookController)
                .build();
    }

    @Test
    @DisplayName("Deve processar resposta '1' (SIM) e enviar para o RabbitMQ")
    void deveProcessarRespostaSim() throws Exception {
        String jsonPayload = """
                {
                    "chat": { "id": "5511999999999" },
                    "msgContent": { "conversation": "1" }
                }
                """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Resposta registrada com sucesso"));

        ArgumentCaptor<AnswerRequest> captor = ArgumentCaptor.forClass(AnswerRequest.class);
        verify(answerProducer).sendAnswer(captor.capture());

        assertThat(captor.getValue().phoneNumber()).isEqualTo("5511999999999");
        assertThat(captor.getValue().accepted()).isTrue();
    }

    @Test
    @DisplayName("Deve processar resposta '2' (NÃO) e enviar para o RabbitMQ")
    void deveProcessarRespostaNao() throws Exception {
        String jsonPayload = """
                {
                    "chat": { "id": "5511988888888" },
                    "msgContent": { "conversation": "2" }
                }
                """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        ArgumentCaptor<AnswerRequest> captor = ArgumentCaptor.forClass(AnswerRequest.class);
        verify(answerProducer).sendAnswer(captor.capture());
        assertThat(captor.getValue().accepted()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando a resposta for inválida")
    void deveRetornarBadRequestParaRespostaInvalida() throws Exception {
        String jsonPayload = """
                {
                    "chat": { "id": "5511977777777" },
                    "msgContent": { "conversation": "Opção Inválida" }
                }
                """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Resposta inválida. Envie '1' para SIM ou '2' para NÃO."));

        verifyNoInteractions(answerProducer);
    }

    @Test
    @DisplayName("Deve retornar 200 mesmo em caso de erro interno conforme lógica da Controller")
    void deveLidarComExcecaoNoProducer() throws Exception {
        String jsonPayload = """
                {
                    "chat": { "id": "5511966666666" },
                    "msgContent": { "conversation": "1" }
                }
                """;

        doThrow(new RuntimeException("Queue offline")).when(answerProducer).sendAnswer(any());

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Resposta registrada com sucesso"));
    }
}