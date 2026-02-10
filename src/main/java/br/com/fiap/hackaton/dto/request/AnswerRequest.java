package br.com.fiap.hackaton.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AnswerRequest(
        @NotBlank
        @Pattern(regexp = "\\d{10,11}", message = "O número de telefone deve conter apenas dígitos e ter entre 10 e 11 caracteres.")
        String phoneNumber,
        @NotNull
        Boolean accepted
) {
}
