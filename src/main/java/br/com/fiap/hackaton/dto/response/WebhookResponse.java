package br.com.fiap.hackaton.dto.response;

import java.time.OffsetDateTime;

public record WebhookResponse(
        String pacienteCns,
        String pacienteName,
        String examHashCode,
        String prestadorName,
        AddressResponse prestadorEndereco,
        OffsetDateTime dataHoraMarcada

) {
}
