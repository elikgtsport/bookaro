package pl.sztukakodu.bookaro.catalog.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//TAKIE testowanie NIE uruchamia serwera testowego, a tam może się dużo namieszać
@WebMvcTest(CatalogController.class)//zaciągnięte zostaną tylko controillery w których mamy endpointy i sa oznaczone jako @RestController
//podajemy zawężony controller, po co reszta? niepotrzebna bo testujemy tylko CatalogController
//@WebMvcTest- ta adnotacja włącza security do testów webowych
@ActiveProfiles("test")
@WithMockUser//wstrzykujemy jakiegoś użytkownika do autoryzacji, inaczej wywala 401 Unauthorized
//domyślna konfiguracja Springa wymaga żeby jednak jakiś user był
class CatalogControllerWebTest {

    @MockBean
    CatalogUseCase catalogUseCase;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void shouldGetAllBooks() throws Exception {

        Book effectiveJava = new Book("Effective Java", 2005, new BigDecimal("99.90"), 50L);
        Book javaInPractice = new Book("Java Concurrency in Practice", 2006, new BigDecimal("109.90"), 50L);

        Mockito.when(catalogUseCase.findAll()).thenReturn(List.of(effectiveJava, javaInPractice));

        //expect
        mockMvc.perform(MockMvcRequestBuilders.get("/catalog"))//metoda służy do wykonania żądania HTTP
                .andDo(print())
                .andExpect(status().isOk())//ok czyli status 200
                .andExpect(jsonPath("$", hasSize(2)));//walidujemy czy nasz gałówna ściezka ($) ma 2 elementy - nadmiarowe
    }

}