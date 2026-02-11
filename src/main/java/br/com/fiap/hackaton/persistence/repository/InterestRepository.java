package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.persistence.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByPacienteCnsAndExamHashCode(String pacienteCns, String examHashCode);

    Optional<Interest> findFirstByExamHashCodeAndIsNotifiedFalseAndNotificationStatusNotOrderByUpdatedAtAsc(String examHashCode, Status status);

    List<Interest> findByNotificationStatusAndNotificationSentAtBefore(Status notificationStatus, OffsetDateTime before);

    Optional<Interest> findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(String phoneNumber, Status notificationStatus);
}
