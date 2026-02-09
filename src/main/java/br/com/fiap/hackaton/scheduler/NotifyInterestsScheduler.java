package br.com.fiap.hackaton.scheduler;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.service.AvailabilityService;
import br.com.fiap.hackaton.service.InterestService;
import br.com.fiap.hackaton.service.NotificationService;
import br.com.fiap.hackaton.service.WhatsAppNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class NotifyInterestsScheduler {

    private final InterestService interestService;

    private final AvailabilityService availabilityService;

    private final WhatsAppNotificationService whatsAppNotificationService;

    List<NotificationService> notificationServices;

    @Scheduled(cron = "0 */5 * * * *") // Executa a cada 5 minutos
    @Async
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
                interest.setNotificationStatus("PENDING");
                interest.setNotificationSentAt(java.time.OffsetDateTime.now());
                interest.setUpdatedAt(java.time.OffsetDateTime.now());
                // persist change
                // using service to save
                interestService.registerPendingNotification(interest);
            }
            availabilityService.updateAvailabilityAsNotified(availability);
        }
    }

    // Verifica notificações pendentes expiradas e avança na fila a cada 10 minutos
    @Scheduled(cron = "0 */10 * * * *")
    @Async
    public void processPendingTimeouts() {
        java.time.OffsetDateTime cutoff = java.time.OffsetDateTime.now().minusMinutes(2);
        java.util.List<Interest> expired = interestService.findPendingNotificationsBefore(cutoff);
        for (Interest i : expired) {
            log.info("Notification timeout for interest id={}", i.getIdInterest());
            // Send timeout message
            whatsAppNotificationService.sendSimpleMessage(i, "⏰ O tempo para confirmar a antecipação da sua consulta expirou. Você foi devolvido à fila e receberá nova oportunidade em breve.");
            
            i.setNotificationStatus("TIMEOUT");
            i.setUpdatedAt(java.time.OffsetDateTime.now());
            // persist
            interestService.rejectNotification(i.getIdInterest());

            // notify next in queue for this exam
            Optional<Interest> next = interestService.findFirstPendingByExamHashCode(i.getExamHashCode());
            next.ifPresent(nextInterest -> {
                var availOpt = availabilityService.findAllAvailable().stream()
                        .filter(a -> a.getExamHashCode().equals(nextInterest.getExamHashCode()))
                        .findFirst();
                availOpt.ifPresent(avail -> {
                    notificationServices.forEach(v -> v.sendNotification(nextInterest, avail));
                    nextInterest.setNotificationStatus("PENDING");
                    nextInterest.setNotificationSentAt(java.time.OffsetDateTime.now());
                    nextInterest.setUpdatedAt(java.time.OffsetDateTime.now());
                    interestService.registerPendingNotification(nextInterest);
                });
            });
        }
    }
}
