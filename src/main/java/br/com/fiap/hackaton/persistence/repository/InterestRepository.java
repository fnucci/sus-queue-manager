package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByPacienteCnsAndExamHashCode(String pacienteCns, String examHashCode);

    Optional<Interest> findFirstByExamHashCodeAndIsNotifiedFalseOrderByUpdatedAtAsc(String examHashCode);

    Optional<Interest> findByNotificationCorrelationId(String notificationCorrelationId);

    Optional<Interest> findFirstByExamHashCodeAndIsNotifiedFalseAndNotificationStatusIsNullOrderByUpdatedAtAsc(String examHashCode);

    java.util.List<Interest> findByNotificationStatusAndNotificationSentAtBefore(String notificationStatus, java.time.OffsetDateTime before);

    Optional<Interest> findFirstByPhoneNumberAndNotificationStatusOrderByUpdatedAtDesc(String phoneNumber, String notificationStatus);
}
