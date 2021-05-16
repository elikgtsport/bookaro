package pl.sztukakodu.bookaro.catalog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@ToString(exclude = "authors")
@RequiredArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Book extends BaseEntity {
    @Column(unique = true) //ta kolumna w bazie danych powinna byc traktowana jako unikalna
    private String title;
    private Integer year;
    private BigDecimal price;
    private Long coverId;
    private Long available;

    //cascade = przy zapisie książki lub aktualizacji od razu aktualizowało autorów
    //dopmyślnie Fetch=LAZY ale występuje problem n+1, obchodzimy go podaniem jednego SQL-a, który pobierze od razu wszystkich autorów
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//przy kazdym pobraniu ksiązki z bazy będziemy pobierali od razu autorów
    @JoinTable
    @JsonIgnoreProperties("books")//wyłączenie z serializacji pola books (nieskończona pętla)
    private Set<Author> authors = new HashSet<>();

    @LastModifiedDate
    //przydatne przy debug
    private LocalDateTime updatedAt;

    public Book(String title, Integer year, BigDecimal price, Long available) {
        this.title = title;
        this.year = year;
        this.price = price;
        this.available = available;
    }

    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }

    public void clearAuthors() {
        Book self = this;
        authors.forEach(author -> author.getBooks().remove(self));
        authors.clear();
    }
}



