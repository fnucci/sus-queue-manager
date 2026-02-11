package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AnswerService {

    private InterestService interestService;

    private AvailabilityService availabilityService;

    private WebhookResponseService webhookResponseService;

    private final List<NotificationService> notificationServiceList;

    public void confirmAnswer(AnswerRequest request) {

        Interest interest = interestService.findInterestByPhoneNumberAndStatus(request.phoneNumber(), Status.PENDING);
        interest.setIsNotified(Boolean.FALSE);
        interest.setNotificationStatus(Status.ACCEPTED);
        interest.setUpdatedAt(OffsetDateTime.now());
        interestService.persist(interest);

        Availability availability = availabilityService.findByInterest(interest);
        availability.setIsAvailable(Boolean.FALSE);
        availability.setInterest(interest);
        availabilityService.persist(availability);

        webhookResponseService.sendReponse(interest, availability);

        String message = "✅ Sua consulta foi *confirmada*! Você comparecerá na data agendada. Obrigado!";
        notificationServiceList.forEach(notificationService -> notificationService.sendSimpleMessage(interest, message));

    }

    public void rejectAnswer(AnswerRequest request) {
        Interest interest = interestService.findInterestByPhoneNumberAndStatus(request.phoneNumber(), Status.PENDING);
        interest.setIsNotified(Boolean.FALSE);
        interest.setNotificationStatus(Status.REJECTED);
        interest.setUpdatedAt(OffsetDateTime.now());
        interestService.persist(interest);

        Availability availability = availabilityService.findByInterest(interest);
        availability.setIsAvailable(Boolean.TRUE);
        //Limpa o interessado da lista
        availability.setInterest(null);
        availabilityService.persist(availability);

        String message = "📅 Entendido! Você continuará na fila. Enviaremos outra oportunidade em breve.";
        notificationServiceList.forEach(notificationService -> notificationService.sendSimpleMessage(interest, message));
    }

}
