package br.com.fiap.hackaton.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InterestRequest(
        @NotBlank
        String pacienteName,
        @NotBlank
        String pacienteCns,
        @NotBlank
        @Pattern(regexp = "\\d{10,11}", message = "O número de telefone deve conter apenas dígitos e ter entre 10 e 11 caracteres.")
        String phoneNumber,
        @NotBlank
        String examName,
        @NotBlank
        String examHashCode
) {
}
