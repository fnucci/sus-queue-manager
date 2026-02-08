package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByPacienteCnsAndExamHashCode(String pacienteCns, String examHashCode);

    Optional<Interest> findFirstByExamHashCodeAndIsNotifiedFalseOrderByUpdatedAtAsc(String examHashCode);
}
