package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.producer.InterestProducer;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interests")
@Slf4j
@AllArgsConstructor
public class InterestController {

    private final InterestProducer interestProducer;

    @PostMapping
    public ResponseEntity<String> registerInterest(@RequestBody @Valid InterestRequest request) {
        log.info("Registrando interesse.");
        interestProducer.sendInterest(request);
        return ResponseEntity.ok("Interesse registrado com sucesso!");
    }

}
