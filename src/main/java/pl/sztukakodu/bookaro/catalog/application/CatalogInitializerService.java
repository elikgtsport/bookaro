package pl.sztukakodu.bookaro.catalog.application;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogInitializerUseCase;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase.CreateBookCommand;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase.UpdateBookCoverCommand;
import pl.sztukakodu.bookaro.catalog.db.AuthorJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Author;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.jpa.BaseEntity;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.application.port.QueryOrderUseCase;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CatalogInitializerService implements CatalogInitializerUseCase {

    private final CatalogUseCase catalog;
    private final ManipulateOrderUseCase placeOrder;
    private final QueryOrderUseCase queryOrder;
    private final AuthorJpaRepository authorRepository;
    private final RestTemplate restTemplate; //klasa służąca do komunikacji z zwenętrznymi serwisami po protokole http, będziemy stąd
    //rozmawiać z książką


    @Override
    @Transactional
    public void initialize() {
        initData();
        placeOrder();
    }

    private void initData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("books.csv").getInputStream()))) {
            CsvToBean<CsvBook> build = new CsvToBeanBuilder<CsvBook>(reader)
                    .withType(CsvBook.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            build.stream().forEach(this::initBook);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse CSV file", e);
        }
    }

    private void initBook(CsvBook csvBook) {
//parse authors
        Set<Long> authors = Arrays
                .stream(csvBook.authors.split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(this::getOrCreateAuthor)
                .map(BaseEntity::getId)
                .collect(Collectors.toSet());

        CreateBookCommand command = new CreateBookCommand(
                csvBook.title,
                authors,
                csvBook.year,
                csvBook.amount,
                50L);
        Book book = catalog.addBook(command);

        catalog.updateBookCover(updateBookCoverCommand(book.getId(), csvBook.thumbnail));
    }

    private UpdateBookCoverCommand updateBookCoverCommand(Long bookId, String thumbnailUrl) {
        ResponseEntity<byte[]> response = restTemplate.exchange(thumbnailUrl, HttpMethod.GET, null, byte[].class);
        String contentType = response.getHeaders().toString();
        return new UpdateBookCoverCommand(bookId, response.getBody(), contentType, "cover");

    }

    //albo pobieramy autroa z bazy danych albo tworzymy nowego
    private Author getOrCreateAuthor(String name) {

        return authorRepository
                .findByNameIgnoreCase(name)
                .orElseGet(() -> authorRepository.save(new Author(name)));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CsvBook {
        @CsvBindByName
        private String title;
        @CsvBindByName
        private String authors;
        @CsvBindByName
        private Integer year;
        @CsvBindByName
        private BigDecimal amount;
        @CsvBindByName
        private String thumbnail;//miniaturka
    }


    private void placeOrder() {
        Book effectiveJava = catalog.findOneByTitle("Effective Java").orElseThrow(() -> new IllegalStateException("Cannot find a book"));
        Book javaPuzzlers = catalog.findOneByTitle("Java Puzzlers").orElseThrow(() -> new IllegalStateException("Cannot find a book"));
        //      Book java = catalog.findOneByTitle("Java. Zadania").orElseThrow(() -> new IllegalStateException("Cannot find a book"));
        //     Book java2 = catalog.findOneByTitle("Java. Zadania").orElseThrow(() -> new IllegalStateException("Cannot find a book"));

        Recipient recipient = Recipient.builder()
                .name("Jan Kowalski")
                .phone("602111666")
                .city("Mysłowice")
                .street("Kwiatowa")
                .email("jan@gmail.com")
                .zipCode("41-400")
                .build();

        ManipulateOrderUseCase.PlaceOrderCommand command = ManipulateOrderUseCase.PlaceOrderCommand
                .builder()
                .recipient(recipient)
                .item(new ManipulateOrderUseCase.OrderItemCommand(effectiveJava.getId(), 16))
                .item(new ManipulateOrderUseCase.OrderItemCommand(javaPuzzlers.getId(), 7))
//                .item(new OrderItemCommand(java.getId(), 1))
//                .item(new OrderItemCommand(java2.getId(), 5))
                .build();

        ManipulateOrderUseCase.PlaceOrderResponse response = placeOrder.placeOrder(command);
        String result = response.handle(
                orderId -> "Created ORDER with id: " + orderId,
                error -> "Failed to created order: " + error
        );
        System.out.println(result);
        queryOrder.findAll()
                .forEach(order -> System.out.println("GOT ORDER WITH TOTAL PRICE: " + order.getFinalPrice() + " DETAILS: " + order));
    }

    private void placeOrder2() {
        Book effectiveJava = catalog.findOneByTitle("Effective Java").orElseThrow(() -> new IllegalStateException("Cannot find a book"));

        Recipient recipient = Recipient.builder()
                .name("Ela")
                .phone("602222222")
                .city("Mysłowice")
                .street("Kwiatowa")
                .email("ela@gmail.com")
                .zipCode("41-000")
                .build();

        ManipulateOrderUseCase.PlaceOrderCommand command = ManipulateOrderUseCase.PlaceOrderCommand
                .builder()
                .recipient(recipient)
                .item(new ManipulateOrderUseCase.OrderItemCommand(effectiveJava.getId(), 2))

                .build();

        ManipulateOrderUseCase.PlaceOrderResponse response = placeOrder.placeOrder(command);
        String result = response.handle(
                orderId -> "Created ORDER with id: " + orderId,
                error -> "Failed to created order " + error
        );
        System.out.println(result);
        queryOrder.findAll()
                .forEach(order -> System.out.println("GOT ORDER WITH TOTAL PRICE: " + order.getFinalPrice() + " DETAILS: " + order));
    }

    private void placeOrder3() {
        Book effectiveJava = catalog.findOneByTitle("Effective Java").orElseThrow(() -> new IllegalStateException("Cannot find a book"));

        Recipient recipient = Recipient.builder()
                .name("Adaś")
                .phone("1223455")
                .city("Mysłowice")
                .street("Kwiatowa")
                .email("adam@gmail.com")
                .zipCode("41-000")
                .build();

        ManipulateOrderUseCase.PlaceOrderCommand command = ManipulateOrderUseCase.PlaceOrderCommand
                .builder()
                .recipient(recipient)
                .item(new ManipulateOrderUseCase.OrderItemCommand(effectiveJava.getId(), 8))

                .build();

        ManipulateOrderUseCase.PlaceOrderResponse response = placeOrder.placeOrder(command);
        String result = response.handle(
                orderId -> "Created ORDER with id: " + orderId,
                error -> "Failed to created order " + error
        );
        log.info(result);

        // list all orders
        queryOrder.findAll()
                .forEach(order -> System.out.println("GOT ORDER WITH TOTAL PRICE: " + order.getFinalPrice() + " DETAILS: " + order));
    }

//    private void initData() {
//
//        Author joshua = new Author("Joshua ", "Bloch");
//        Author neal = new Author("Neal", "Gafter");
//        Author darwin = new Author("Ian", "Darwin");
//        authorRepository.save(joshua); //podczas zapisu do bazy Hibernate otwiera nową sesję i zamyka (LazyInitializationException)
//        authorRepository.save(neal); //otwiera nową sesję i zamyka LazyInitializationException
//        authorRepository.save(darwin); //otwiera nową sesję i zamyka LazyInitializationException
//
//        CatalogUseCase.CreateBookCommand effectiveJava = new CatalogUseCase.CreateBookCommand("Effective Java", Set.of(joshua.getId()), 2005, new BigDecimal("79.00"), 50L);
//        CatalogUseCase.CreateBookCommand effectiveJava_2010 = new CatalogUseCase.CreateBookCommand("Effective Java", Set.of(joshua.getId()), 2010, new BigDecimal("79.00"), 50L);
//        CatalogUseCase.CreateBookCommand javaPuzzlers = new CatalogUseCase.CreateBookCommand("Java Puzzlers", Set.of(joshua.getId(), neal.getId()), 2018, new BigDecimal("99.00"), 50L);
//        CatalogUseCase.CreateBookCommand javaZadania = new CatalogUseCase.CreateBookCommand("Java. Zadania", Set.of(darwin.getId()), 2020, new BigDecimal("15.00"), 50L);
//
////        catalog.addBook(new CatalogUseCase.CreateBookCommand("Harry Potter i Komnata tajemnic",1998, new BigDecimal("10.00")));
////        catalog.addBook(new CatalogUseCase.CreateBookCommand("Sezon burz",  2013, new BigDecimal("10.00")));
////        catalog.addBook(new CatalogUseCase.CreateBookCommand("Pan Tadeusz",  1987, new BigDecimal("10.00")));
////        catalog.addBook(new CatalogUseCase.CreateBookCommand("Ogniem i mieczem", 1854, new BigDecimal("10.00")));
////        catalog.addBook(new CatalogUseCase.CreateBookCommand("Pan Wołodyjowski", 1864, new BigDecimal("10.00")));
////        catalog.addBook(new CatalogUseCase.CreateBookCommand("Chłopi", 1864, new BigDecimal("10.00")));
//
//        catalog.addBook(effectiveJava);//otwiera nową sesję. Hibernate otwiera sobie sesję na czas działania z bazą danych
//        catalog.addBook(javaPuzzlers);//otwiera nową sesję
//        catalog.addBook(javaZadania);
//        catalog.addBook(effectiveJava_2010);
//    }
}
