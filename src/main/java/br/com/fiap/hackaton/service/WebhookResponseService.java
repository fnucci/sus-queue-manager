package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.presenter.WebhookResponsePresenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class WebhookResponseService {

    private RestClient restClient;

    @Value("${app.webhook.response.url}")
    private String webhookResponseUrl;

    public WebhookResponseService(RestClient.Builder restClient) {
        this.restClient = restClient.build();
    }

    @Async
    public void sendReponse(Interest interest, Availability availability) {
        log.info("Enviando resposta ao SUS interest id {} availability id {}", interest.getIdInterest(), availability.getIdAvailability());
        //Simula a devolução do endpoint de confirmação da consulta, que poderia ser um sistema externo ou interno
        var webhookResponse = restClient.post()
                .uri(webhookResponseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(WebhookResponsePresenter.toResponse(interest, availability)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    // Log do erro sem estourar exceção para o fluxo principal
                    log.error("Falha no Webhook: status {}", response.getStatusCode());
                })
                .toBodilessEntity();

        if(webhookResponse.getStatusCode() == HttpStatusCode.valueOf(200)) {
            log.info("Resposta do Webhook enviada com sucesso para interest id {} availability id {}", interest.getIdInterest(), availability.getIdAvailability());
        }
    }
}
