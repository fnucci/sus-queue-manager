package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.producer.AnswerProducer;
import br.com.fiap.hackaton.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook/whatsapp")
@AllArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final AnswerProducer answerProducer;

    @PostMapping
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> body) {
        log.info("Webhook recebido: {}", body);

        try {
            // Extrair telefone do chat
            String phoneNumber = "";
            if (body.containsKey("chat") && body.get("chat") instanceof Map) {
                Map chat = (Map) body.get("chat");
                if (chat.containsKey("id")) {
                    phoneNumber = String.valueOf(chat.get("id"));
                }
            }

            // Extrair mensagem de msgContent
            String incomingText = "";
            if (body.containsKey("msgContent") && body.get("msgContent") instanceof Map) {
                Map msgContent = (Map) body.get("msgContent");
                if (msgContent.containsKey("conversation")) {
                    incomingText = String.valueOf(msgContent.get("conversation")).trim();
                }
            }

            log.info("Telefone extraído: {}, Mensagem: {}", phoneNumber, incomingText);

            //Valida se a resposta do paciente é 1 (SIM) ou 2 (NÃO) e envia para RabbitMQ
            Boolean answer;
            switch (incomingText) {
                case "1":
                    answer = Boolean.TRUE;
                    break;
                case "2":
                    answer = Boolean.FALSE;
                    break;
                default:
                    log.warn("Resposta inválida recebida: {}. Esperado '1' para SIM ou '2' para NÃO.", incomingText);
                    return ResponseEntity.badRequest().body("Resposta inválida. Envie '1' para SIM ou '2' para NÃO.");
            }

            AnswerRequest answerRequest = new AnswerRequest(phoneNumber, answer);
            answerProducer.sendAnswer(answerRequest);
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok("Resposta registrada com sucesso");
    }
}