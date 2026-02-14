package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.producer.AvailabilityProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AvailabilityControllerTest {

    @Mock // Substitui o antigo @MockBean
    private AvailabilityProducer availabilityProducer;

    @InjectMocks
    private AvailabilityController availabilityController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(availabilityController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("Deve registrar disponibilidade com sucesso usando as APIs modernas do Spring 3.4+")
    void deveRegistrarDisponibilidade() throws Exception {

        String requestBody = """
                {
                   "prestadorName": "Hospital Central de Diagnósticos",
                   "prestadorEndereco": {
                     "street": "Avenida Paulista",
                     "number": "1000",
                     "neighborhood": "Bela Vista",
                     "city": "São Paulo",
                     "state": "SP",
                     "zipCode": "01310-100"
                   },
                   "examName": "Tomografia computadorizada",
                   "examHashCode": "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911",
                   "dataHoraDisponivel": "2026-02-15T15:30:00"
                 }""";

        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());


    }

    @Test
    @DisplayName("Deve retornar 400 quando o body for inválido (Bean Validation)")
    void deveRetornarErroValidacao() throws Exception {
        // Envia um JSON vazio para disparar as restrições do @Valid
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

