package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.dto.request.AnswerRequest;
import br.com.fiap.hackaton.producer.AnswerProducer;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/answer")
@Slf4j
@AllArgsConstructor
public class AnswerController {

    private final AnswerProducer answerProducer;

    @PostMapping
    public ResponseEntity<String> recieve(@RequestBody @Valid AnswerRequest request) {
        log.info("Registrando resposta do usuario a notificação.");
        answerProducer.sendAnswer(request);
        return ResponseEntity.ok("Resposta registrada com sucesso!");
    }
}
