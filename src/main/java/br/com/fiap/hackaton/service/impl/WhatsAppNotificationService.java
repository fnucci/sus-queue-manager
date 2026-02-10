package br.com.fiap.hackaton.service.impl;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import br.com.fiap.hackaton.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
public class WhatsAppNotificationService implements NotificationService {

    private final WebClient webClient;
    private final InterestRepository interestRepository;
    private final Logger log = LoggerFactory.getLogger(WhatsAppNotificationService.class);
    @Value("${wapi.instanceId}")
    private String instanceId;

    public WhatsAppNotificationService(WebClient wapiWebClient, InterestRepository interestRepository) {
        this.webClient = wapiWebClient;
        this.interestRepository = interestRepository;
    }

    @Override
    public void sendNotification(Interest interest, Availability availability) {
        String correlationId = UUID.randomUUID().toString();
        interest.setNotificationCorrelationId(correlationId);
        interest.setUpdatedAt(OffsetDateTime.now());
        if (interestRepository != null) {
            interestRepository.save(interest);
        } else {
            log.warn("InterestRepository is null — skipping persist of correlationId for interest={}", interest.getIdInterest());
        }

        String message = buildNotificationMessage(interest, availability);

        Map<String, Object> payload = Map.of(
            "phone", safe(interest.getPhoneNumber()),
            "message", safe(message),
            "delayMessage", 15
        );

        if (webClient != null) {
            try {
                Map response = webClient.post()
                        .uri(uriBuilder -> uriBuilder.path("/message/send-text").queryParam("instanceId", instanceId).build())
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                log.info("W-API response: {}", response);
            } catch (Exception e) {
                log.error("Falha ao enviar mensagem via W-API: {}", e.getMessage(), e);
            }
        } else {
            log.warn("WebClient is null — skipping call to W-API. Payload={}", payload);
        }
    }

    private String buildNotificationMessage(Interest interest, Availability availability) {
        return String.format(
                "Olá %s, o exame %s foi disponibilizado por %s em %s. Data: %s\n\n" +
                "Responda:\n" +
                "*1* para confirmar a consulta\n" +
                "*2* para rejeitar a consulta",
                safe(interest.getPacienteName()),
                safe(interest.getExamName()),
                safe(availability.getPrestadorName()),
                safe(availability.getPrestadorEndereco() != null ? availability.getPrestadorEndereco().getCity() : null),
                formatDate(availability.getDataHoraDisponivel())
        );
    }

    private String formatDate(OffsetDateTime dateTime) {
        if (dateTime == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }

    @Override
    public void sendSimpleMessage(Interest interest, String message) {
        if (interest == null || interest.getPhoneNumber() == null) {
            log.warn("Cannot send message: interest or phone number is null");
            return;
        }

        Map<String, Object> payload = Map.of(
                "phone", safe(interest.getPhoneNumber()),
                "message", safe(message),
                "delayMessage", 0
        );

        if (webClient != null) {
            try {
                Map response = webClient.post()
                        .uri(uriBuilder -> uriBuilder.path("/message/send-text").queryParam("instanceId", instanceId).build())
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                log.info("Response message sent to {}: {}", interest.getPhoneNumber(), response);
            } catch (Exception e) {
                log.error("Falha ao enviar mensagem simples via W-API: {}", e.getMessage(), e);
            }
        } else {
            log.warn("WebClient is null — skipping response message. Message={}", message);
        }
    }
}
