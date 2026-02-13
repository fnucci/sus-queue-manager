package br.com.fiap.hackaton.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record WebhookResponse(
        @NotBlank
        String pacienteCns,
        @NotBlank
        String pacienteName,
        @NotBlank
        String examHashCode,
        @NotBlank
        String prestadorName,
        @NotNull
        AddressResponse prestadorEndereco,
        @NotNull
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}Z$")
        LocalDateTime dataHoraMarcada

) {
}
