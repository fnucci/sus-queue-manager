package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Interest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestRepositoryTest {

    @Mock
    private InterestRepository interestRepository;

    @Test
    @DisplayName("Deve simular a busca por paciente CNS e Exam Hash Code com sucesso")
    void deveSimularBuscaPorCnsEHash() {
        // Arrange
        String cns = "123456789";
        String hash = "HASH-TESTE-001";
        Interest mockInterest = new Interest();
        mockInterest.setPacienteCns(cns);
        mockInterest.setExamHashCode(hash);

        // Configuramos o Mock para retornar o objeto quando os parâmetros coincidirem
        when(interestRepository.findByPacienteCnsAndExamHashCode(cns, hash))
                .thenReturn(Optional.of(mockInterest));

        // Act
        Optional<Interest> result = interestRepository.findByPacienteCnsAndExamHashCode(cns, hash);

        // Assert
        assertTrue(result.isPresent(), "O resultado deveria conter um Interest");
        assertEquals(cns, result.get().getPacienteCns());
        assertEquals(hash, result.get().getExamHashCode());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o mock for configurado para tal")
    void deveRetornarVazioNoMock() {
        // Arrange
        when(interestRepository.findByPacienteCnsAndExamHashCode("000", "EMPTY"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Interest> result = interestRepository.findByPacienteCnsAndExamHashCode("000", "EMPTY");

        // Assert
        assertTrue(result.isEmpty());
    }
}
