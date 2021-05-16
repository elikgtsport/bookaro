package pl.sztukakodu.bookaro.catalog.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


//zewnętrzy test Api który stawia serwer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //stawia prawdziwy serwerna losowym porcie
@AutoConfigureTestDatabase
class CatalogControllerApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate; //możemy wykonać prawdziwe żądanie http do systemu i zwalidować odpowiedź

    @MockBean
    CatalogUseCase catalogUseCase; //chowamy wartwę aplikacyjna za Mockami, nie używamy tu H2

    @Test
    public void getAllBook() {
        //given
        Book effectiveJava = new Book("Effective Java", 2005, new BigDecimal("99.90"), 50L);
        Book javaInPractice = new Book("Java Concurrency in Practice", 2006, new BigDecimal("109.90"), 50L);

        Mockito.when(catalogUseCase.findAll()).thenReturn(List.of(effectiveJava, javaInPractice));
        //when
        ParameterizedTypeReference<List<Book>> type = new ParameterizedTypeReference<>() {
        };
        String url = "http://localhost:" + port + "/catalog";
        RequestEntity<Void> request = RequestEntity.get(URI.create(url)).build();
        ResponseEntity<List<Book>> response = restTemplate.exchange(request, type);

//then
        assertEquals(2, response.getBody().size());

    }


}