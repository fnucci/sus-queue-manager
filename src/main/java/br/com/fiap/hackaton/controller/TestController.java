package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import br.com.fiap.hackaton.scheduler.NotifyInterestsScheduler;
import br.com.fiap.hackaton.service.InterestService;
import br.com.fiap.hackaton.service.impl.WhatsAppNotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private NotifyInterestsScheduler scheduler;
    
    @Autowired
    private InterestService interestService;
    
    @Autowired
    private WhatsAppNotificationService whatsAppService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Dispara o scheduler para enviar notificações para todos os pacientes em espera
     */
    @PostMapping("/notify-interests")
    public ResponseEntity<Map<String, String>> triggerNotify() {
        try {
            scheduler.notifyInterests();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notificações disparadas com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Processa timeouts de notificações com 2 horas
     */
    @PostMapping("/process-timeouts")
    public ResponseEntity<Map<String, String>> triggerTimeouts() {
        try {
            scheduler.processPendingTimeouts();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Timeouts processados com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Simula resposta do usuário (1=SIM ou 2=NÃO)
     * 
     * Exemplo:
     * POST http://localhost:8080/test/simulate-response/1?interestId=123&response=1
     * 
     * Se response=1 (SIM): Confirma a consulta
     * Se response=2 (NÃO): Rejeita e avança para o próximo paciente
     */
    @PostMapping("/simulate-response")
    public ResponseEntity<Map<String, Object>> simulateResponse(
            @RequestParam Long interestId,
            @RequestParam Integer response) {
        
        try {
            Interest interest = interestService.findById(interestId);
            if (interest == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Interest não encontrado"
                ));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("interestId", interestId);
            result.put("pacientName", interest.getPacienteName());
            result.put("phone", interest.getPhoneNumber());

            if (response == 1) {
                // SIM: Confirmar a consulta
                interest.setNotificationStatus(Status.ACCEPTED);
                interest.setIsNotified(true);
                interestService.save(interest);
                
                // Enviar mensagem de confirmação
                whatsAppService.sendSimpleMessage(
                    interest,
                    "✅ Sua consulta foi *confirmada* para " + formatDate(interest) + 
                    ".\n\nAguardamos você!"
                );
                
                // Publicar no RabbitMQ
                rabbitTemplate.convertAndSend("ANSWER_EXCHANGE", "answer.confirmed", interest);
                
                result.put("status", "success");
                result.put("action", "CONSULTA CONFIRMADA");
                result.put("message", "Resposta 1 (SIM) registrada - Consulta antecipada confirmada!");
                
            } else if (response == 2) {
                // NÃO: Rejeitar e avançar para próximo
                interest.setNotificationStatus(Status.REJECTED);
                interestService.save(interest);
                
                // Enviar mensagem de rejeição
                whatsAppService.sendSimpleMessage(
                    interest,
                    "📅 Entendido! Você continuará na fila. Enviaremos outra oportunidade em breve."
                );
                
                // Publicar no RabbitMQ
                rabbitTemplate.convertAndSend("ANSWER_EXCHANGE", "answer.rejected", interest);
                
                // Avançar para o próximo paciente
                scheduler.notifyInterests();
                
                result.put("status", "success");
                result.put("action", "CONSULTA REJEITADA");
                result.put("message", "Resposta 2 (NÃO) registrada - Próximo paciente será notificado!");
                
            } else {
                return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Response inválido. Use 1 (SIM) ou 2 (NÃO)"
                ));
            }

            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Listar todos os Interests para saber qual ID usar
     */
    @GetMapping("/interests-list")
    public ResponseEntity<?> listInterests() {
        try {
            var interests = interestService.findAll();
            return ResponseEntity.ok(interests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    private String formatDate(Interest interest) {
        // Aqui você pode formatar a data da consulta conforme necessário
        return "09/02/2026 19:45";
    }
}
