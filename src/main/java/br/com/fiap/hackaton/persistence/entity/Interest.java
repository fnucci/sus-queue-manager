package br.com.fiap.hackaton.persistence.entity;

import br.com.fiap.hackaton.dto.request.InterestRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = "interest")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInterest;;

    private String pacientName;

    private String pacienteCns;

    private String phoneNumber;

    private String examName;

    private String examHashCode;

    private String notificationCorrelationId;

    private String notificationStatus; // PENDING, ACCEPTED, REJECTED, TIMEOUT

    private java.time.OffsetDateTime notificationSentAt;

    private Boolean isNotified;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public Interest (InterestRequest interestRequest) {
        this.pacientName = interestRequest.pacienteName();
        this.pacienteCns = interestRequest.pacienteCns();
        this.phoneNumber = interestRequest.phoneNumber();
        this.examName = interestRequest.examName();
        this.examHashCode = interestRequest.examHashCode();
        this.isNotified = Boolean.FALSE;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void setNotificationCorrelationId(String notificationCorrelationId) {
        this.notificationCorrelationId = notificationCorrelationId;
    }

    public String getNotificationCorrelationId() {
        return notificationCorrelationId;
    }
}
