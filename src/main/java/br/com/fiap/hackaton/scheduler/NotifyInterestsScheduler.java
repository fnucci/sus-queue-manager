package br.com.fiap.hackaton.scheduler;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import br.com.fiap.hackaton.service.AvailabilityService;
import br.com.fiap.hackaton.service.InterestService;
import br.com.fiap.hackaton.service.NotificationService;
import br.com.fiap.hackaton.service.impl.WhatsAppNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class NotifyInterestsScheduler {

    private final InterestService interestService;

    private final AvailabilityService availabilityService;

    List<NotificationService> notificationServices;

    @Scheduled(cron = "0 */5 * * * *") // Executa a cada 5 minutos
    public void notifyInterests() {
        // Lógica para notificar os pacientes sobre as novas disponibilidades de exames
        log.info("Obtem todas as agendas disponiuveis no banco de dados");
        List<Availability> availabilityList = availabilityService.findAllAvailable();

        for (Availability availability : availabilityList) {
            log.info("Obtem todos os interesses relacionados ao exame e que ainda não foram notificados");
            Optional<Interest> interestOptional = interestService.findFirstInterestByExamHashCodeAndIsNotifiedFalseOrderByUpdatedAtAsc(availability.getExamHashCode());
            log.info("Notifica via algum canal, whatsapp, email, sms, etc");
            if (interestOptional.isPresent()){
                Interest interest = interestOptional.get();
                // send question and mark as PENDING
                this.notificationServices.forEach(v -> v.sendNotification(interest, availability));

                // atualiza o interesse para PENDING
                interest.setNotificationStatus(Status.PENDING);
                interest.setNotificationSentAt(OffsetDateTime.now());
                interest.setUpdatedAt(OffsetDateTime.now());
                interest.setIsNotified(Boolean.TRUE);
                // persiste change o interest
                interestService.persist(interest);

                //atualiza a availability para não disponível e associa o interesse
                availability.setInterest(interest);
                availability.setIsAvailable(Boolean.FALSE);

                //persiste a change da availability
                availabilityService.persist(availability);
            }
        }
    }

    // Verifica notificações pendentes expiradas e avança na fila a cada 10 minutos
    @Scheduled(cron = "0 */10 * * * *")
    public void processPendingTimeouts() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(2);
        List<Interest> expired = interestService.findPendingNotificationsBefore(cutoff);
        for (Interest interest : expired) {
            log.info("Notification timeout for interest id={}", interest.getIdInterest());
            String message = "⏰ O tempo para confirmar a antecipação da sua consulta expirou. Você foi devolvido à fila e receberá nova oportunidade em breve.";            // Send timeout message
            this.notificationServices.forEach(v -> v.sendSimpleMessage(interest, message));

            interest.setNotificationStatus(Status.TIMEOUT);
            interest.setUpdatedAt(OffsetDateTime.now());
            // persist
            interestService.persist(interest);

            //Libera a disponibilidade para o próximo da fila
            Availability availability = availabilityService.findByInterest(interest);
            availability.setIsAvailable(Boolean.TRUE);
            //Limpa o interessado da lista
            availability.setInterest(null);
            availabilityService.persist(availability);

        }
    }
}
