package br.com.fiap.hackaton.persistence.entity;

import br.com.fiap.hackaton.dto.request.AvailabilityRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAvailability;

    private String prestadorName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address prestadorEndereco;

    private String examHashCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime dataHoraDisponivel;

    private Boolean isAvailable;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "interest_id", referencedColumnName = "idInterest")
    private Interest interest;

    public Availability(AvailabilityRequest request) {
        this.prestadorName = request.prestadorName();
        this.prestadorEndereco = new Address(request.prestadorEndereco());
        this.examHashCode = request.examHashCode();
        this.dataHoraDisponivel = request.dataHoraDisponivel();
        this.isAvailable = Boolean.TRUE;
    }
}
