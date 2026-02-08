package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.custom.InterestAlreadyRegisteredException;
import br.com.fiap.hackaton.exception.custom.InterestNotFoundException;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

    public Optional<Interest> findFirstInterestByExamHashCodeAndIsNotifiedFalseOrderByUpdatedAtAsc(String examHashCode) {
        return interestRepository.findFirstByExamHashCodeAndIsNotifiedFalseOrderByUpdatedAtAsc(examHashCode);
    }

    public void updateInterestAsNotified(Interest interest) {
        log.info("Atualiza o interesse como notificado");
        interest.setIsNotified(Boolean.TRUE);
        interest.setUpdatedAt(OffsetDateTime.now());
        interestRepository.save(interest);
    }

    public void rejectNotification(Long interestId) {
        log.info("Rejeita a notificação do interesse com id {}", interestId);
        Interest interest = findInterestById(interestId);

        interest.setIsNotified(Boolean.FALSE);
        interest.setUpdatedAt(OffsetDateTime.now());
        interestRepository.save(interest);
    }

    public Interest findInterestById(Long interestId) {
        Optional<Interest> interestOptional = interestRepository.findById(interestId);

        if (interestOptional.isPresent()) {
            return interestOptional.get();
        }
        else{
            log.info("Interesse com id {} não encontrado", interestId);
            throw new InterestNotFoundException(interestId);
        }
    }
}
