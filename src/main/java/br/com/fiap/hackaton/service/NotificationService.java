package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;

public interface NotificationService {

    public void sendNotification(Interest interest, Availability availability);

    public void sendSimpleMessage(Interest interest, String message);
}
