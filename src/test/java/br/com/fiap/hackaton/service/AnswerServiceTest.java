package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private InterestService interestService;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private WebhookResponseService webhookResponseService;

    @Mock
    private NotificationService notificationService;

    @Spy
    private List<NotificationService> notificationServiceList = new ArrayList<>();

    @InjectMocks
    private AnswerService answerService;

    private Interest interest;
    private Availability availability;
    private AnswerRequest answerRequest;

    @BeforeEach
    void setUp() {
        notificationServiceList.add(notificationService);

        answerRequest = new AnswerRequest("5511999999999", true);

        interest = new Interest();
        interest.setPhoneNumber("5511999999999");
        interest.setNotificationStatus(Status.PENDING);

        availability = new Availability();
        availability.setInterest(interest);
        availability.setIsAvailable(false);
    }

    @Test
    @DisplayName("Deve confirmar a resposta, atualizar status para ACCEPTED e notificar sucesso")
    void confirmAnswer_Sucesso() {
        // Arrange
        when(interestService.findInterestByPhoneNumberAndStatus(answerRequest.phoneNumber(), Status.PENDING))
                .thenReturn(interest);
        when(availabilityService.findByInterest(interest)).thenReturn(availability);

        // Act
        answerService.confirmAnswer(answerRequest);

        // Assert
        assertEquals(Status.ACCEPTED, interest.getNotificationStatus());
        assertFalse(interest.getIsNotified());
        verify(interestService).persist(interest);

        assertFalse(availability.getIsAvailable());
        assertEquals(interest, availability.getInterest());
        verify(availabilityService).persist(availability);

        verify(webhookResponseService).sendReponse(interest, availability);
        verify(notificationService).sendSimpleMessage(eq(interest), anyString());
    }

    @Test
    @DisplayName("Deve rejeitar a resposta, atualizar status para REJECTED e liberar a vaga")
    void rejectAnswer_Sucesso() {
        // Arrange
        when(interestService.findInterestByPhoneNumberAndStatus(answerRequest.phoneNumber(), Status.PENDING))
                .thenReturn(interest);
        when(availabilityService.findByInterest(interest)).thenReturn(availability);

        // Act
        answerService.rejectAnswer(answerRequest);

        // Assert
        assertEquals(Status.REJECTED, interest.getNotificationStatus());
        verify(interestService).persist(interest);

        assertTrue(availability.getIsAvailable());
        assertNull(availability.getInterest());
        verify(availabilityService).persist(availability);

        verify(notificationService).sendSimpleMessage(eq(interest), anyString());
        verifyNoInteractions(webhookResponseService);
    }
}