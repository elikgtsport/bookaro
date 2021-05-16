package pl.sztukakodu.bookaro.order.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UpdateStatusResult {

    OrderStatus newStatus; //nowy nadany status - po zmianie z anluowanego albo porzuconego
    boolean revoked;  //jesli bedzie na true to trzeba będzie przywrócić ksiązki do systemu
//    boolean paid; //jesli będzie na true, to nie będzie można anulowac zamówienia
//    boolean shipped; //jesli będzie na true, to nie będzie można anulowac zamówienia

    static UpdateStatusResult ok(OrderStatus newStatus) {
        return new UpdateStatusResult(newStatus, false);
    }

    //przywracamy ksiązki do sprzedazy bo zostały porzucone lub anulowano zakup(revoked==true)
    static UpdateStatusResult revoked(OrderStatus newStatus) {
        return new UpdateStatusResult(newStatus, true);
    }
}
