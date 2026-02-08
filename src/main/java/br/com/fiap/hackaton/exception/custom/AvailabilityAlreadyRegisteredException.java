package br.com.fiap.hackaton.exception.custom;

public class AvailabilityAlreadyRegisteredException extends RuntimeException {
    public AvailabilityAlreadyRegisteredException() {
        super("Vaga já registrada para o exame no dia e horario informados.");
    }
}
