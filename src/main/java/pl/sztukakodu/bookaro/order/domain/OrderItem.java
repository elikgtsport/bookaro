package pl.sztukakodu.bookaro.order.domain;

import lombok.*;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrderItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "book_id")//dołączamy kolumnę w której id tej ksiazki się będzie znajdowało
    private Book book;//lepiej trzymac cała ksiązke przy walidacji przyda sie, wiele zamowień może być zmapowane do jednej encji ksiązki

    private int quantity;

}
