package br.com.fiap.hackaton.exception.custom;

public class AvailabilityNotFountException extends RuntimeException {
    public AvailabilityNotFountException(Long id) {
      super(String.format("A disponibilidade do exame id %d não foi encontrada.", id));
    }
}
