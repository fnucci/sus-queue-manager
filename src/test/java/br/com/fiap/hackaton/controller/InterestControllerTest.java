package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.producer.InterestProducer;
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
class InterestControllerTest {

    @Mock // Substitui o antigo @MockBean
    private InterestProducer interestProducerMock;

    @InjectMocks
    private InterestController interestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(interestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("Deve registrar interesse com sucesso usando as APIs modernas do Spring 3.4+")
    void deveRegistrarInteresse() throws Exception {

        String requestBody = """
                {
                        "pacienteName": "teste",
                        "pacienteCns": "2139847582914",
                        "phoneNumber": "11981048890",
                        "examName": "Tomografia computadorizada",
                        "examHashCode": "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911"
                }""";

        mockMvc.perform(post("/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());


    }

    @Test
    @DisplayName("Deve retornar 400 quando o body for inválido (Bean Validation)")
    void deveRetornarErroValidacao() throws Exception {
        // Envia um JSON vazio para disparar as restrições do @Valid
        mockMvc.perform(post("/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}