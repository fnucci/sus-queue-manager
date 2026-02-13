package br.com.fiap.hackaton.persistence.entity;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = "interest")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInterest;
    ;

    private String pacienteName;

    private String pacienteCns;

    private String phoneNumber;

    private String examName;

    private String examHashCode;

    private String notificationCorrelationId;

    private Status notificationStatus; // PENDING, ACCEPTED, REJECTED, TIMEOUT

    private LocalDateTime notificationSentAt;

    private Boolean isNotified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Interest(InterestRequest interestRequest) {
        this.pacienteName = interestRequest.pacienteName();
        this.pacienteCns = interestRequest.pacienteCns();
        //Foi alterado para respeitar a api do whatsapp, que exige o código do país no número de telefone. No caso do Brasil, o código é 55.
        this.phoneNumber = String.format("55%s", interestRequest.phoneNumber());
        this.examName = interestRequest.examName();
        this.examHashCode = interestRequest.examHashCode();
        this.isNotified = Boolean.FALSE;
        this.notificationStatus = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
