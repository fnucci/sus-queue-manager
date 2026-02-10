package br.com.fiap.hackaton.presenter;

import br.com.fiap.hackaton.dto.response.AddressResponse;
import br.com.fiap.hackaton.dto.response.WebhookResponse;
import br.com.fiap.hackaton.persistence.entity.Address;
import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;

public class WebhookResponsePresenter {

    public static WebhookResponse toResponse(Interest interest, Availability availability) {
        return new WebhookResponse(interest.getPacienteCns(), interest.getPacienteName(), availability.getExamHashCode(), availability.getPrestadorName(), toAddressResponse(availability.getPrestadorEndereco()), availability.getDataHoraDisponivel());
    }

    private static AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(address.getStreet(), address.getNumber(), address.getNeighborhood(), address.getCity(), address.getState(), address.getZipCode());
    }
}
