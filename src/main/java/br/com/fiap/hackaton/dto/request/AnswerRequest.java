package br.com.fiap.hackaton.dto.request;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull
        Long interestId,
        @NotNull
        Long availabilityId,
        @NotNull
        Boolean accepted
) {
}
