package br.com.fiap.hackaton.scheduler;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import br.com.fiap.hackaton.service.AvailabilityService;
import br.com.fiap.hackaton.service.InterestService;
import br.com.fiap.hackaton.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifyInterestsSchedulerTest {

    @Mock
    private InterestService interestService;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private NotificationService notificationService; // Mock de um serviço de notificação

    @Spy
    private List<NotificationService> notificationServices = new ArrayList<>();

    @InjectMocks
    private NotifyInterestsScheduler scheduler;

    @BeforeEach
    void setup() {
        // Como a classe usa uma lista injetada, adicionamos o mock à lista espiada
        notificationServices.add(notificationService);
    }

    @Test
    @DisplayName("Deve processar notificações e atualizar estados quando houver disponibilidade e interesse")
    void notifyInterests_Sucesso() {
        // Arrange
        Availability availability = new Availability();
        availability.setExamHashCode("HASH123");
        availability.setIsAvailable(true);

        Interest interest = new Interest();
        interest.setPacienteCns("123");

        when(availabilityService.findAllAvailable()).thenReturn(List.of(availability));
        when(interestService.findFirstInterestByExamHashCodeAndIsNotifiedFalseAndNotificationStatusNotAcceptedOrderByUpdatedAtAsc("HASH123"))
                .thenReturn(Optional.of(interest));

        // Act
        scheduler.notifyInterests();

        // Assert
        verify(notificationService).sendNotification(eq(interest), eq(availability));

        // Verificações de mudança de estado no Interest
        assertEquals(Status.PENDING, interest.getNotificationStatus());
        assertTrue(interest.getIsNotified());
        verify(interestService).persist(interest);

        // Verificações de mudança de estado na Availability
        assertFalse(availability.getIsAvailable());
        assertEquals(interest, availability.getInterest());
        verify(availabilityService).persist(availability);
    }

    @Test
    @DisplayName("Deve processar timeouts de notificações expiradas")
    void processPendingTimeouts_Sucesso() {
        // Arrange
        Interest interestExpired = new Interest();
        interestExpired.setIdInterest(1L);
        interestExpired.setNotificationStatus(Status.PENDING);

        Availability availability = new Availability();
        availability.setInterest(interestExpired);
        availability.setIsAvailable(false);

        when(interestService.findPendingNotificationsBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(interestExpired));
        when(availabilityService.findByInterest(interestExpired)).thenReturn(availability);

        // Act
        scheduler.processPendingTimeouts();

        // Assert
        verify(notificationService).sendSimpleMessage(eq(interestExpired), anyString());

        // Verifica se o interesse voltou para a fila (isNotified = false)
        assertEquals(Status.TIMEOUT, interestExpired.getNotificationStatus());
        assertFalse(interestExpired.getIsNotified());
        verify(interestService).persist(interestExpired);

        // Verifica se a disponibilidade foi liberada
        assertTrue(availability.getIsAvailable());
        assertNull(availability.getInterest());
        verify(availabilityService).persist(availability);
    }

    @Test
    @DisplayName("Não deve fazer nada se não houver disponibilidades")
    void notifyInterests_SemDisponibilidade() {
        // Arrange
        when(availabilityService.findAllAvailable()).thenReturn(List.of());

        // Act
        scheduler.notifyInterests();

        // Assert
        verify(interestService, never()).findFirstInterestByExamHashCodeAndIsNotifiedFalseAndNotificationStatusNotAcceptedOrderByUpdatedAtAsc(anyString());
        verifyNoInteractions(notificationService);
    }
}