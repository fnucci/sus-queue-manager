package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import br.com.fiap.hackaton.exception.custom.AvailabilityAlreadyRegisteredException;
import br.com.fiap.hackaton.exception.custom.InterestAlreadyRegisteredException;
import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.AvailabilityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    @Transactional
    public void registerAvailability(AvailabilityRequest availabilityRequest) {
        log.info("Busca se ja há alguma vaga para o mesmo exame salvo no banco de dados");
        Optional<Availability> existingAvailability = availabilityRepository.findByExamHashCodeAndDataHoraDisponivel(availabilityRequest.examHashCode(), availabilityRequest.dataHoraDisponivel());

        if (existingAvailability.isPresent()) {
            log.info("Vaga já registrada para a data {} e exame {}", availabilityRequest.dataHoraDisponivel(), availabilityRequest.examHashCode());
            throw new AvailabilityAlreadyRegisteredException();
        }

        Availability availability = new Availability(availabilityRequest);

        log.info("Persiste a vaga no banco de dados");
        availabilityRepository.save(availability);

    }
}
