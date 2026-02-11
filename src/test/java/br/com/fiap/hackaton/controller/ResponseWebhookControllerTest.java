package br.com.fiap.hackaton.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ResponseWebhookControllerTest {

    @InjectMocks
    private ResponseWebhookController responseController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(responseController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("Deve simular o funcionamento do webhook com sucesso usando as APIs modernas do Spring 3.4+")
    void deveRegistrarResposta() throws Exception {

        String requestBody = """
                {
                    "pacienteCns": "123456789012345",
                    "pacienteName": "João da Silva",
                    "examHashCode": "EX-998877-ABC",
                    "prestadorName": "Hospital Central de Diagnósticos",
                    "prestadorEndereco": {
                      "street": "Avenida Paulista",
                      "number": "1100",
                      "neighborhood": "Bela Vista",
                      "city": "São Paulo",
                      "state": "SP",
                      "zipCode": "01310-100"
                    },
                    "dataHoraMarcada": "2026-02-15T19:30Z"
                  }""";

        mockMvc.perform(post("/webhook/response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}