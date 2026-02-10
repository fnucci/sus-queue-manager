package br.com.fiap.hackaton.persistence.repository;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    Optional<Availability> findByExamHashCodeAndDataHoraDisponivel(String examHashCode, OffsetDateTime dataHoraDisponivel);

    List<Availability> findAllByIsAvailableTrue();

    Optional<Availability> findByInterest(Interest interest);
}
