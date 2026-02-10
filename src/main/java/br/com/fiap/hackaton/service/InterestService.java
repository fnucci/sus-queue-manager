package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import br.com.fiap.hackaton.exception.custom.InterestAlreadyRegisteredException;
import br.com.fiap.hackaton.exception.custom.InterestNotFoundException;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import br.com.fiap.hackaton.persistence.repository.InterestRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
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
        return interestRepository.findFirstByExamHashCodeAndIsNotifiedFalseAndNotificationStatusIsNullOrderByUpdatedAtAsc(examHashCode);
    }

    public Optional<Interest> findFirstPendingByExamHashCode(String examHashCode) {
        return interestRepository.findFirstByExamHashCodeAndIsNotifiedFalseAndNotificationStatusIsNullOrderByUpdatedAtAsc(examHashCode);
    }

    public List<Interest> findPendingNotificationsBefore(java.time.OffsetDateTime before) {
        return interestRepository.findByNotificationStatusAndNotificationSentAtBefore(Status.PENDING, before);
    }

    public void updateInterestAsNotified(Interest interest) {
        log.info("Atualiza o interesse como notificado");
        interest.setIsNotified(Boolean.TRUE);
        interest.setUpdatedAt(OffsetDateTime.now());
        interestRepository.save(interest);
    }

    public void persist(Interest interest) {
        log.info("Persiste o interestcom id {}", interest.getIdInterest());
        interestRepository.save(interest);
    }

    public void registerPendingNotification(Interest interest) {
        log.info("Marca interesse {} como PENDING", interest.getIdInterest());
        interestRepository.save(interest);
    }

    public Interest findInterestById(Long interestId) {
        Optional<Interest> interestOptional = interestRepository.findById(interestId);

        if (interestOptional.isPresent()) {
            return interestOptional.get();
        } else {
            log.info("Interesse com id {} não encontrado", interestId);
            throw new InterestNotFoundException();
        }
    }

    public Interest findInterestByPhoneNumberAndStatus(String phoneNumber, Status status) {
        Optional<Interest> interestOptional = interestRepository.findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(phoneNumber, status);

        if (interestOptional.isPresent()) {
            return interestOptional.get();
        } else {
            log.info("Interesse com telefone {} e status {} não encontrado", phoneNumber, status);
            throw new InterestNotFoundException();
        }
    }

    public Interest findById(Long interestId) {
        return findInterestById(interestId);
    }

    public Interest save(Interest interest) {
        return interestRepository.save(interest);
    }

    public List<Interest> findAll() {
        return interestRepository.findAll();
    }
}
