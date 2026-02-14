package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
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
class AvailabilityRepositoryTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Test
    @DisplayName("Deve simular a busca por Hash do Exame e Data/Hora com sucesso")
    void deveSimularBuscaPorHashEData() {
        // Arrange
        String hash = "EXAM-123";
        LocalDateTime dataHora = LocalDateTime.now();
        Availability mockAvailability = new Availability();
        mockAvailability.setExamHashCode(hash);
        mockAvailability.setDataHoraDisponivel(dataHora);

        when(availabilityRepository.findByExamHashCodeAndDataHoraDisponivel(hash, dataHora))
                .thenReturn(Optional.of(mockAvailability));

        // Act
        Optional<Availability> result = availabilityRepository.findByExamHashCodeAndDataHoraDisponivel(hash, dataHora);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(hash, result.get().getExamHashCode());
        assertEquals(dataHora, result.get().getDataHoraDisponivel());
    }

    @Test
    @DisplayName("Deve simular a listagem de todas as disponibilidades ativas")
    void deveSimularBuscaPorDisponibilidadeAtiva() {
        // Arrange
        Availability availableItem = new Availability();
        availableItem.setIsAvailable(true);

        when(availabilityRepository.findAllByIsAvailableTrue())
                .thenReturn(List.of(availableItem));

        // Act
        List<Availability> result = availabilityRepository.findAllByIsAvailableTrue();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsAvailable());
    }

    @Test
    @DisplayName("Deve simular a busca de disponibilidade vinculada a um Interesse")
    void deveSimularBuscaPorInteresse() {
        // Arrange
        Interest mockInterest = new Interest();
        mockInterest.setIdInterest(1L);

        Availability mockAvailability = new Availability();
        mockAvailability.setInterest(mockInterest);

        when(availabilityRepository.findByInterest(mockInterest))
                .thenReturn(Optional.of(mockAvailability));

        // Act
        Optional<Availability> result = availabilityRepository.findByInterest(mockInterest);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockInterest, result.get().getInterest());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando buscar por interesse inexistente")
    void deveRetornarVazioParaInteresseInexistente() {
        // Arrange
        when(availabilityRepository.findByInterest(any(Interest.class)))
                .thenReturn(Optional.empty());

        // Act
        Optional<Availability> result = availabilityRepository.findByInterest(new Interest());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver itens disponíveis")
    void deveRetornarListaVaziaQuandoNaoHouverDisponiveis() {
        // Arrange
        when(availabilityRepository.findAllByIsAvailableTrue())
                .thenReturn(Collections.emptyList());

        // Act
        List<Availability> result = availabilityRepository.findAllByIsAvailableTrue();

        // Assert
        assertTrue(result.isEmpty());
    }
}