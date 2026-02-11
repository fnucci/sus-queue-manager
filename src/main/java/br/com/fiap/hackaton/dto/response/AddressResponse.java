package br.com.fiap.hackaton.dto.response;

import jakarta.validation.constraints.NotBlank;

public record AddressResponse(
        @NotBlank
        String street,
        @NotBlank
        String number,
        @NotBlank
        String neighborhood,
        @NotBlank
        String city,
        @NotBlank
        String state,
        @NotBlank
        String zipCode
) {
}
