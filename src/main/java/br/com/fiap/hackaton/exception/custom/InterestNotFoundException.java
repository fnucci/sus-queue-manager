package br.com.fiap.hackaton.exception.custom;

public class InterestNotFoundException extends RuntimeException {
    public InterestNotFoundException(Long interestId) {
        super(String.format("O interesse com id informado %d não foi encontrado", interestId));
    }
}
