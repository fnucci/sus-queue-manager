package br.com.fiap.hackaton.controller;

import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import br.com.fiap.hackaton.producer.AvailabilityProducer;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/availability")
@Slf4j
@AllArgsConstructor
public class AvailabilityController {

    private final AvailabilityProducer availabilityProducer;

    @PostMapping
    public ResponseEntity<String> registerInterest(@RequestBody @Valid AvailabilityRequest request) {
        log.info("Registrando interesse.");
        availabilityProducer.sendAvailability(request);
        return ResponseEntity.ok("Interesse registrado com sucesso!");
    }
}
