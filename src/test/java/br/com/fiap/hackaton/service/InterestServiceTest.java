package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.custom.InterestAlreadyRegisteredException;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @InjectMocks
    private InterestService interestService;

    @Test
    @DisplayName("Deve registrar um interesse com sucesso quando não houver duplicidade")
    void deveRegistrarInteresseComSucesso() {
        // Arrange
        InterestRequest request = createRequest();

        // Simula que não encontrou nenhum interesse existente
        when(interestRepository.findByPacienteCnsAndExamHashCode(request.pacienteCns(), request.examHashCode()))
                .thenReturn(Optional.empty());

        // Act
        interestService.registerInterest(request);

        // Assert
        verify(interestRepository, times(1)).save(any(Interest.class));
    }

    @Test
    @DisplayName("Deve lançar InterestAlreadyRegisteredException quando o interesse já existir")
    void deveLancarExcecaoQuandoInteresseJaExistir() {
        // Arrange
        InterestRequest request = createRequest();
        Interest existingInterest = new Interest(request);

        // Simula que o repositório encontrou um registro
        when(interestRepository.findByPacienteCnsAndExamHashCode(request.pacienteCns(), request.examHashCode()))
                .thenReturn(Optional.of(existingInterest));

        // Act & Assert
        assertThrows(InterestAlreadyRegisteredException.class, () -> {
            interestService.registerInterest(request);
        });

        // Garante que o save NUNCA foi chamado
        verify(interestRepository, never()).save(any(Interest.class));
    }

    private InterestRequest createRequest() {
        // Ajuste os parâmetros de acordo com o seu Record/DTO
        return new InterestRequest("Teste", "1234567890", "11981048435", "Tomografia teste", "ausdjhyrfgawijdfbuhwgfg83724194g");
    }
}
