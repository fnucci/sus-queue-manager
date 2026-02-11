package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.custom.InterestAlreadyRegisteredException;
import br.com.fiap.hackaton.exception.custom.InterestNotFoundException;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @InjectMocks
    private InterestService interestService;

    @Test
    @DisplayName("Deve registrar um novo interesse com sucesso")
    void deveRegistrarInteresseComSucesso() {
        // Arrange
        InterestRequest request = new InterestRequest(
                "Paciente Teste", "123456", "11999999999",
                "Raio X", "HASH-EXAME-001"
        );

        when(interestRepository.findByPacienteCnsAndExamHashCode(request.pacienteCns(), request.examHashCode()))
                .thenReturn(Optional.empty());

        // Act
        interestService.registerInterest(request);

        // Assert
        verify(interestRepository, times(1)).save(any(Interest.class));
    }

    @Test
    @DisplayName("Deve lançar InterestAlreadyRegisteredException quando o interesse já existir")
    void deveLancarExecaoQuandoInteresseJaExistir() {
        // Arrange
        InterestRequest request = new InterestRequest(
                "Paciente Teste", "123456", "11999999999",
                "Raio X", "HASH-EXAME-001"
        );
        Interest interestExistente = new Interest();

        when(interestRepository.findByPacienteCnsAndExamHashCode(request.pacienteCns(), request.examHashCode()))
                .thenReturn(Optional.of(interestExistente));

        // Act & Assert
        assertThrows(InterestAlreadyRegisteredException.class, () -> {
            interestService.registerInterest(request);
        });

        verify(interestRepository, never()).save(any(Interest.class));
    }

    @Test
    @DisplayName("Deve retornar o interesse quando encontrado por telefone e status")
    void deveEncontrarInteressePorTelefoneEStatus() {
        // Arrange
        String telefone = "11999999999";
        Status status = Status.PENDING;
        Interest interestMock = new Interest();
        interestMock.setPhoneNumber(telefone);

        when(interestRepository.findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(telefone, status))
                .thenReturn(Optional.of(interestMock));

        // Act
        Interest result = interestService.findInterestByPhoneNumberAndStatus(telefone, status);

        // Assert
        assertNotNull(result);
        assertEquals(telefone, result.getPhoneNumber());
    }

    @Test
    @DisplayName("Deve lançar InterestNotFoundException quando o interesse não for encontrado por telefone")
    void deveLancarExceptionQuandoTelefoneNaoEncontrado() {
        // Arrange
        String telefone = "000000000";
        Status status = Status.PENDING;

        when(interestRepository.findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(telefone, status))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InterestNotFoundException.class, () -> {
            interestService.findInterestByPhoneNumberAndStatus(telefone, status);
        });
    }

    @Test
    @DisplayName("Deve chamar o repository para persistir um Interest diretamente")
    void devePersistirInterest() {
        // Arrange
        Interest interest = new Interest();

        // Act
        interestService.persist(interest);

        // Assert
        verify(interestRepository).save(interest);
    }
}