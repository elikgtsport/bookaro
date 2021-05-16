package pl.sztukakodu.bookaro.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.util.UUID;
@Getter
@Setter
@EqualsAndHashCode(of = "uuid") //ma się opierać tylko o uuid
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    //tworzymy sztuczne id żeby nie było draki w db
    private Long id;

    private String uuid = UUID.randomUUID().toString();

    @Version//służy do ustawiania wersji przy konflitach podczas aktualizacji danych, wyjątki do obsługi
    //pomocne przy współbiezności, kied dwa procesy będą chciały mieć dostęp do tego samego zasobu to tylko jeden się uda
    private Long version;

}
