package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.custom.InterestAlreadyRegisteredException;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class InterestService {

    private final InterestRepository interestRepository;

    @Transactional
    public void registerInterest(InterestRequest interestRequest) {
        log.info("Busca se ja há algum interesse salvo no banco de dados");
        Optional<Interest> existingInterest = interestRepository.findByPacienteCnsAndExamHashCode(interestRequest.pacienteCns(), interestRequest.examHashCode());

        if (existingInterest.isPresent()) {
            log.info("Interesse já registrado para o paciente {} e exame {}", interestRequest.pacienteCns(), interestRequest.examHashCode());

            throw new InterestAlreadyRegisteredException();
        }

        Interest interest = new Interest(interestRequest);

        log.info("Persiste o Interest no banco de dados");
        interestRepository.save(interest);
    }
}
