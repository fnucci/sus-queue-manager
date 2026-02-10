package br.com.fiap.hackaton.exception.custom;

public class InterestNotFoundException extends RuntimeException {
    public InterestNotFoundException() {
        super("O interesse informado não foi encontrado");
    }
}
