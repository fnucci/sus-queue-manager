package br.com.fiap.hackaton.persistence.entity;

import br.com.fiap.hackaton.dto.request.AddressRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String street;
    private String number;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;


    public Address (AddressRequest request){
        this.street = request.street();
        this.number = request.number();
        this.neighborhood = request.neighborhood();
        this.city = request.city();
        this.state = request.state();
        this.zipCode = request.zipCode();
    }
}
