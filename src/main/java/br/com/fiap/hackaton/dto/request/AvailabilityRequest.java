package br.com.fiap.hackaton.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record AvailabilityRequest(
        @NotBlank
        String prestadorName,
        @NotNull
        AddressRequest prestadorEndereco,
        @NotBlank
        String examName,
        @NotBlank
        String examHashCode,
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @Future
        OffsetDateTime dataHoraDisponivel
) {
}
