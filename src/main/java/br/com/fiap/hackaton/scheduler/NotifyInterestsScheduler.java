package br.com.fiap.hackaton.scheduler;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.service.AvailabilityService;
import br.com.fiap.hackaton.service.InterestService;
import br.com.fiap.hackaton.service.NotificationService;
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
            if (interestOptional. isPresent()){
                Interest interest = interestOptional.get();
                this.notificationServices.forEach(v -> v.sendNotification(interest, availability));
                interestService.updateInterestAsNotified(interest);
            }
            availabilityService.updateAvailabilityAsNotified(availability);
        }
    }
}
