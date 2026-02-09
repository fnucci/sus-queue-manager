package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.config.RabbitConfig;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import br.com.fiap.hackaton.service.AvailabilityService;
import br.com.fiap.hackaton.service.InterestService;
import br.com.fiap.hackaton.service.NotificationService;
import br.com.fiap.hackaton.service.WhatsAppNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    private final InterestRepository interestRepository;
    private final InterestService interestService;
    private final AvailabilityService availabilityService;
    private final List<NotificationService> notificationServices;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final RabbitTemplate rabbitTemplate;
    private final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    public WhatsAppWebhookController(InterestRepository interestRepository,
                                     InterestService interestService,
                                     AvailabilityService availabilityService,
                                     List<NotificationService> notificationServices,
                                     WhatsAppNotificationService whatsAppNotificationService,
                                     RabbitTemplate rabbitTemplate) {
        this.interestRepository = interestRepository;
        this.interestService = interestService;
        this.availabilityService = availabilityService;
        this.notificationServices = notificationServices;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<Void> handleCallback(@RequestBody Map<String, Object> body) {
        log.info("Webhook recebido: {}", body);

        try {
            // Extrair telefone do chat
            String phoneNumber = null;
            if (body.containsKey("chat") && body.get("chat") instanceof Map) {
                Map chat = (Map) body.get("chat");
                if (chat.containsKey("id")) {
                    phoneNumber = String.valueOf(chat.get("id"));
                }
            }

            // Extrair mensagem de msgContent
            String incomingText = null;
            if (body.containsKey("msgContent") && body.get("msgContent") instanceof Map) {
                Map msgContent = (Map) body.get("msgContent");
                if (msgContent.containsKey("conversation")) {
                    incomingText = String.valueOf(msgContent.get("conversation"));
                }
            }

            log.info("Telefone extraído: {}, Mensagem: {}", phoneNumber, incomingText);

            if (phoneNumber != null) {
                // Buscar o Interest mais recente pelo número de telefone com status PENDING
                Optional<Interest> opt = interestRepository
                    .findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(phoneNumber, "PENDING");

                if (opt.isPresent()) {
                    Interest interest = opt.get();
                    log.info("Interest encontrado: {}", interest.getIdInterest());

                    // Handle incoming replies (text)
                    if (incomingText != null) {
                        String normalized = incomingText.trim();
                        if ("1".equals(normalized)) {
                            log.info("Resposta 1 (SIM) recebida para interest {}", interest.getIdInterest());
                            // accepted
                            interestService.markNotificationAccepted(interest.getIdInterest());
                            whatsAppNotificationService.sendSimpleMessage(interest, 
                                "✅ Sua consulta foi *confirmada*! Você comparecerá na data agendada. Obrigado!");
                            rabbitTemplate.convertAndSend(RabbitConfig.ANSWER_EXCHANGE, 
                                RabbitConfig.ROUTING_KEY_ANSWER_CONFIRMED, interest);
                            log.info("Confirmação enviada para {}", phoneNumber);
                        } else if ("2".equals(normalized)) {
                            log.info("Resposta 2 (NÃO) recebida para interest {}", interest.getIdInterest());
                            // rejected
                            interestService.rejectNotification(interest.getIdInterest());
                            whatsAppNotificationService.sendSimpleMessage(interest, 
                                "📅 Entendido! Você continuará na fila. Enviaremos outra oportunidade em breve.");
                            rabbitTemplate.convertAndSend(RabbitConfig.ANSWER_EXCHANGE, 
                                RabbitConfig.ROUTING_KEY_ANSWER_REJECTED, interest);
                            // notify next
                            notifyNextForExam(interest.getExamHashCode());
                            log.info("Próximo paciente será notificado para exame {}", interest.getExamHashCode());
                        } else {
                            log.warn("Resposta inválida recebida: {}. Apenas 1 ou 2 são aceitos.", normalized);
                        }
                    }
                } else {
                    log.warn("Nenhum Interest encontrado para telefone {} com status PENDING", phoneNumber);
                }
            } else {
                log.warn("Número de telefone não encontrado no webhook");
            }
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    private void notifyNextForExam(String examHashCode) {
        Optional<Interest> nextOpt = interestService.findFirstPendingByExamHashCode(examHashCode);
        nextOpt.ifPresent(next -> {
            // find availability for exam
            var availOpt = availabilityService.findAllAvailable().stream().filter(a -> a.getExamHashCode().equals(next.getExamHashCode())).findFirst();
            availOpt.ifPresent(avail -> {
                notificationServices.forEach(n -> n.sendNotification(next, avail));
                next.setNotificationStatus("PENDING");
                next.setNotificationSentAt(java.time.OffsetDateTime.now());
                next.setUpdatedAt(java.time.OffsetDateTime.now());
                interestService.registerPendingNotification(next);
            });
        });
    }
}
