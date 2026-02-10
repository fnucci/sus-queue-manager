package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.dto.response.WebhookResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class ResponseWebhookController {
    @PostMapping("/webhook/response")
    public ResponseEntity<WebhookResponse> handleWebhook(@RequestBody WebhookResponse response) {
        log.info("Webhook de resposta recebido: {}", response);
        // Sempre retorne um status 200 OK rapidamente
        return ResponseEntity.ok(response);

    }
}
