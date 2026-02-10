package br.com.fiap.hackaton.dto.response;

public record AddressResponse(
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String zipCode
) {
}
