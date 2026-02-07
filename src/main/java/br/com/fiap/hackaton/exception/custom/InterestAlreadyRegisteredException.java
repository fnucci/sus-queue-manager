package br.com.fiap.hackaton.exception.custom;

public class InterestAlreadyRegisteredException extends RuntimeException {
    public InterestAlreadyRegisteredException() {
        super("Interesse já registrado para o paciente e exame informados");
    }
}
