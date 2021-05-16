package pl.sztukakodu.bookaro.catalog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase.CreateBookCommand;
import pl.sztukakodu.bookaro.catalog.db.AuthorJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Author;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest //stawiamy cały kontekst Springa
@AutoConfigureTestDatabase//Spring wykrywa, że ma dostępną testową bazę danych h2 w pom.xml i ją użyje do testów
//po odpaleniu testów, operacje zostały wykonane na bazie danych w pamięci która jest wypełniana danycmi od nowa
//bo początkowo zawsze jest pusta
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)//oznaczamy testy jako brudzące kontekst Springa, wszystkie beany będa tworzone od nowa a baza danych czyszczona do 0
class CatalogControllerIT {

    @Autowired //wstrzykujemy obiekty (nie trzeba tworzyć konstruktorów i odpowiednich zależności)
    AuthorJpaRepository authorJpaRepository;
    @Autowired
    CatalogController catalogController;
    @Autowired
    CatalogUseCase catalogUseCase;

    @Test
    public void getAllBooks() {

        givenEffectiveJava();
        givenJavaConcurrencyInPractice();

        List<Book> all = catalogController.getAll(Optional.empty(), Optional.empty());
        assertEquals(2, all.size());
    }

    @Test
    public void getBooksByAuthor() {

        givenEffectiveJava();
        givenJavaConcurrencyInPractice();

        List<Book> byAuthor = catalogController.getAll(Optional.empty(), Optional.of("Bloch"));
        assertEquals(1, byAuthor.size());
        assertEquals("Effective Java", byAuthor.get(0).getTitle());
    }

    @Test
    public void getBooksByTitle() {

        givenEffectiveJava();
        givenJavaConcurrencyInPractice();

        List<Book> byAuthor = catalogController.getAll(Optional.of("Java Concurrency in Practice"), Optional.empty());
        assertEquals(1, byAuthor.size());
        assertEquals("Java Concurrency in Practice", byAuthor.get(0).getTitle());
    }
    private void givenJavaConcurrencyInPractice() {
        Author goetz = authorJpaRepository.save(new Author("Brian Goetz"));
        catalogUseCase.addBook(
                new CreateBookCommand(
                        "Java Concurrency in Practice",
                        Set.of(goetz.getId()),
                        2006,
                        new BigDecimal("100.90"),
                        60L));
    }

    private void givenEffectiveJava() {
        Author joshua_bloch = authorJpaRepository.save(new Author("Joshua Bloch"));
        catalogUseCase.addBook(
                new CreateBookCommand(
                        "Effective Java",
                        Set.of(joshua_bloch.getId()),
                        2005,
                        new BigDecimal("99.90"),
                        50L));
    }

}