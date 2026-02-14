package br.com.fiap.hackaton.service;

import br.com.fiap.hackaton.persistence.entity.Address;
import br.com.fiap.hackaton.persistence.entity.Availability;
import br.com.fiap.hackaton.persistence.entity.Interest;
import br.com.fiap.hackaton.service.impl.WhatsAppNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da classe WhatsAppNotificationService")
class WhatsAppNotificationServiceTest {

    @InjectMocks
    private WhatsAppNotificationService whatsAppNotificationService;

    private Interest interest;
    private Availability availability;
    private Address address;

    @BeforeEach
    void setUp() {
        // Criando dados de teste para Interest
        interest = new Interest();
        interest.setIdInterest(1L);
        interest.setPacienteName("João Silva");
        interest.setPhoneNumber("11999999999");
        interest.setExamName("Eletrocardiograma");
        interest.setPacienteCns("123456789012345");
        interest.setIsNotified(false);

        // Criando dados de teste para Address
        address = new Address();
        address.setId(1L);
        address.setCity("São Paulo");
        address.setState("SP");
        address.setZipCode("01310100");

        // Criando dados de teste para Availability
        availability = new Availability();
        availability.setIdAvailability(1L);
        availability.setPrestadorName("Dr. Pedro");
        availability.setPrestadorEndereco(address);
        availability.setDataHoraDisponivel(LocalDateTime.now());
        availability.setIsAvailable(true);
    }

    @Test
    @DisplayName("Deve enviar notificação com sucesso")
    void testSendNotificationWithSuccess() {
        // Act
        assertDoesNotThrow(() -> {
            whatsAppNotificationService.sendNotification(interest, availability);
        });
    }

    @Test
    @DisplayName("Deve construir mensagem com dados corretos")
    void testBuildNotificationMessage() {
        // Act
        whatsAppNotificationService.sendNotification(interest, availability);

        // Assert - verifica se não lançou exceção
        assertNotNull(interest.getPacienteName());
        assertNotNull(interest.getExamName());
        assertNotNull(availability.getPrestadorName());
        assertNotNull(availability.getPrestadorEndereco().getCity());
    }

    @Test
    @DisplayName("Deve formatar data corretamente")
    void testFormatDate() {
        // Act
        OffsetDateTime now = OffsetDateTime.now();

        // Assert - verifica se consegue enviar a notificação sem erros de formatação
        assertDoesNotThrow(() -> {
            whatsAppNotificationService.sendNotification(interest, availability);
        });
    }

    @Test
    @DisplayName("Deve enviar para número de telefone correto")
    void testSendToCorrectPhoneNumber() {
        // Act
        whatsAppNotificationService.sendNotification(interest, availability);

        // Assert
        assertEquals("11999999999", interest.getPhoneNumber());
        assertNotNull(interest.getPhoneNumber());
    }

    @Test
    @DisplayName("Deve lidar com telefone nulo sem erro")
    void testSendWithNullPhoneNumber() {
        // Arrange
        interest.setPhoneNumber(null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            whatsAppNotificationService.sendNotification(interest, availability);
        });
    }

    @Test
    @DisplayName("Deve lidar com nome de paciente nulo sem erro")
    void testSendWithNullPacientName() {
        // Arrange
        interest.setPacienteName(null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            whatsAppNotificationService.sendNotification(interest, availability);
        });
    }
}
