package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestRepositoryTest {

    @Mock
    private InterestRepository interestRepository;

    @Test
    @DisplayName("Deve simular a busca pelo primeiro registro não notificado por Hash")
    void deveSimularBuscaPrimeiroNaoNotificado() {
        // Arrange
        String hash = "HASH-123";
        Status statusExceto = Status.ACCEPTED;
        Interest mockInterest = new Interest();
        mockInterest.setExamHashCode(hash);
        mockInterest.setIsNotified(false);

        when(interestRepository.findFirstByExamHashCodeAndIsNotifiedFalseAndNotificationStatusNotOrderByUpdatedAtAsc(hash, statusExceto))
                .thenReturn(Optional.of(mockInterest));

        // Act
        Optional<Interest> result = interestRepository
                .findFirstByExamHashCodeAndIsNotifiedFalseAndNotificationStatusNotOrderByUpdatedAtAsc(hash, statusExceto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(hash, result.get().getExamHashCode());
    }

    @Test
    @DisplayName("Deve simular a listagem de notificações por status e data anterior")
    void deveSimularBuscaPorStatusEDataAnterior() {
        // Arrange
        Status status = Status.PENDING;
        LocalDateTime dataLimite = LocalDateTime.now();
        Interest mockInterest = new Interest();
        mockInterest.setNotificationStatus(status);

        when(interestRepository.findByNotificationStatusAndNotificationSentAtBefore(status, dataLimite))
                .thenReturn(List.of(mockInterest));

        // Act
        List<Interest> result = interestRepository.findByNotificationStatusAndNotificationSentAtBefore(status, dataLimite);

        // Assert
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getNotificationStatus());
    }

    @Test
    @DisplayName("Deve simular a busca pela última resposta por telefone e status")
    void deveSimularBuscaUltimaRespostaPorTelefone() {
        // Arrange
        String telefone = "5511999999999";
        Status status = Status.PENDING;
        Interest mockInterest = new Interest();
        mockInterest.setPhoneNumber(telefone);
        mockInterest.setNotificationStatus(status);

        when(interestRepository.findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(telefone, status))
                .thenReturn(Optional.of(mockInterest));

        // Act
        Optional<Interest> result = interestRepository.findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(telefone, status);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(telefone, result.get().getPhoneNumber());
        assertEquals(status, result.get().getNotificationStatus());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver notificações pendentes")
    void deveRetornarListaVaziaNoMock() {
        // Arrange
        when(interestRepository.findByNotificationStatusAndNotificationSentAtBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        List<Interest> result = interestRepository.findByNotificationStatusAndNotificationSentAtBefore(Status.PENDING, LocalDateTime.now());

        // Assert
        assertTrue(result.isEmpty());
    }
}