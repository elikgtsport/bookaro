package pl.sztukakodu.bookaro.catalog.web;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class) //Junit ze springiem żeby gadał (MockBean)
@ContextConfiguration(classes = {CatalogController.class})
//przekazujemy co do sztuki jakie beany(klasy konfiguracyjne) chcemy utworzyć
public class CatalogControllerTest {

    @MockBean
    CatalogUseCase catalogUseCase;

    @Autowired
    CatalogController catalogController;

    @Test
    public void schouldGetAllBooks() {
        //given
        Book effectiveJava = new Book("Effective Java", 2005, new BigDecimal("99.90"), 50L);
        Book javaInPractice = new Book("Java Concurrency in Practice", 2006, new BigDecimal("109.90"), 50L);

        //when
        Mockito.when(catalogUseCase.findAll()).thenReturn(List.of(effectiveJava, javaInPractice));
        List<Book> all = catalogController.getAll(Optional.empty(), Optional.empty());
        //then
        assertEquals(2, all.size());
    }
}
