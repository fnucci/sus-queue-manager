package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.AddressRequest;
import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import br.com.fiap.hackaton.exception.custom.AvailabilityAlreadyRegisteredException;
import br.com.fiap.hackaton.exception.custom.AvailabilityNotFountException;
import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.AvailabilityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private InterestService interestService; // Injetado via AllArgsConstructor

    @InjectMocks
    private AvailabilityService availabilityService;

    @Test
    @DisplayName("Deve registrar uma disponibilidade com sucesso")
    void deveRegistrarDisponibilidadeComSucesso() {
        // Arrange
        LocalDateTime data = LocalDateTime.now();
        AvailabilityRequest request = new AvailabilityRequest("TestePrestador", new AddressRequest("Rua teste", "103", "Vila da telha", "Sao Paulo", "SP", "04833101"), "Tomografia Computadorizada", "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911", data); // Presumindo construtor padrão

        when(availabilityRepository.findByExamHashCodeAndDataHoraDisponivel(request.examHashCode(), request.dataHoraDisponivel()))
                .thenReturn(Optional.empty());

        // Act
        availabilityService.registerAvailability(request);

        // Assert
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    @DisplayName("Deve lançar AvailabilityAlreadyRegisteredException quando a vaga já existir")
    void deveLancarExcecaoQuandoVagaJaExistir() {
        // Arrange
        LocalDateTime data = LocalDateTime.now();
        AvailabilityRequest request = new AvailabilityRequest("TestePrestador", new AddressRequest("Rua teste", "103", "Vila da telha", "Sao Paulo", "SP", "04833101"), "Tomografia Computadorizada", "205c0ec4dd914cecb1a166d1d72434e6a1f1fceda26220b14d75b533afea6911", data); // Presumindo construtor padrão

        when(availabilityRepository.findByExamHashCodeAndDataHoraDisponivel(request.examHashCode(), request.dataHoraDisponivel()))
                .thenReturn(Optional.of(new Availability()));

        // Act & Assert
        assertThrows(AvailabilityAlreadyRegisteredException.class, () -> {
            availabilityService.registerAvailability(request);
        });

        verify(availabilityRepository, never()).save(any(Availability.class));
    }

    @Test
    @DisplayName("Deve retornar todas as vagas disponíveis")
    void deveRetornarTodasAsVagasDisponiveis() {
        // Arrange
        List<Availability> mockList = List.of(new Availability(), new Availability());
        when(availabilityRepository.findAllByIsAvailableTrue()).thenReturn(mockList);

        // Act
        List<Availability> result = availabilityService.findAllAvailable();

        // Assert
        assertEquals(2, result.size());
        verify(availabilityRepository).findAllByIsAvailableTrue();
    }

    @Test
    @DisplayName("Deve encontrar disponibilidade por interesse")
    void deveEncontrarPorInteresse() {
        // Arrange
        Interest interest = new Interest();
        Availability availability = new Availability();
        when(availabilityRepository.findByInterest(interest)).thenReturn(Optional.of(availability));

        // Act
        Availability result = availabilityService.findByInterest(interest);

        // Assert
        assertNotNull(result);
        verify(availabilityRepository).findByInterest(interest);
    }

    @Test
    @DisplayName("Deve lançar AvailabilityNotFountException quando não encontrar por interesse")
    void deveLancarExcecaoQuandoInteresseNaoEncontrado() {
        // Arrange
        Interest interest = new Interest();
        when(availabilityRepository.findByInterest(interest)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AvailabilityNotFountException.class, () -> {
            availabilityService.findByInterest(interest);
        });
    }

    @Test
    @DisplayName("Deve persistir uma availability diretamente")
    void devePersistirAvailability() {
        // Arrange
        Availability availability = new Availability();
        availability.setIdAvailability(10L);

        // Act
        availabilityService.persist(availability);

        // Assert
        verify(availabilityRepository).save(availability);
    }
}