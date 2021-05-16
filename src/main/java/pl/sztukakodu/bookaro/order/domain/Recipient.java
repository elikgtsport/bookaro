package pl.sztukakodu.bookaro.order.domain;

import lombok.*;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.Entity;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Embeddable//pola zostają zapisane w bazie danych w ramach tej encji, w której Recipient jest używany
//Wszystkie pola z recipient znajdą się jako kolumny w bazie w tabeli orders
@Entity
public class Recipient extends BaseEntity {

    private String email;//zakładamy ze każdy odbiorca ma unikalny adres e-mail
    private String name;
    private String phone;
    private String street;
    private String city;
    private String zipCode;

}
