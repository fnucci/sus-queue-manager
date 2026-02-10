package br.com.fiap.hackaton.exception.custom;

public class AvailabilityNotFountException extends RuntimeException {
    public AvailabilityNotFountException() {
      super("A disponibilidade do exame id %d não foi encontrada.");
    }
}
