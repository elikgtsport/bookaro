package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.clock.Clock;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.application.port.QueryOrderUseCase;
import pl.sztukakodu.bookaro.order.domain.Delivery;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.*;

@SpringBootTest(properties = "app.orders.payment-period=1H")
@AutoConfigureTestDatabase
class AbandonedOrdersJobTest {

    @TestConfiguration
    //podczas uruchamiania testów spring zajrzy tu do srodka i wykorzysta beany zawarte w tej klasie
    static class TestConfig {
        @Bean
        public Clock.Fake clock() {
            return new Clock.Fake();
        }
    }

    @Autowired
    AbandonedOrdersJob abandonedOrdersJob;

    @Autowired
    BookJpaRepository bookJpaRepository;

    @Autowired
    ManipulateOrderService manipulateOrderService;

    @Autowired
    QueryOrderUseCase queryOrderUseCase;

    @Autowired
    CatalogUseCase catalogUseCase;

    @Autowired
    Clock.Fake clock;

    @Test
    public void shouldMarkOrderAsAbandoned() {
        //given
        Book javaConcurrency = givenJavaConcurrency(50L);
        Long orderId = placeOrder(javaConcurrency.getId(), 15);
        //when
        //przesuwamy zegar o 2h do przodu tak żeby sztucznie wywołać żeby zamówienie stało się przestarzałe
        //wprowadziliśmy abstarkcję clocka
        clock.tick(Duration.ofHours(2));
        //sprawdzamy czy po wykonaniu tego zadania status się zmieni i liczba książek wróciła spowrotem do 50
        abandonedOrdersJob.run();

        OrderStatus status = queryOrderUseCase.findById(orderId).get().getStatus();
        //then
        assertEquals(OrderStatus.ABANDONED, status);
        assertEquals(50L, availableCopiesOfBooks(javaConcurrency));//książki wracają do systemu


        /*oryginalnie w klasie AbandonedOrdersJob mamy:
        Duration paymentPeriod = properties.getPaymentPeriod();
        trzeba te propertosy sobie wstrzyknąć bo nie będziemy czekać 5 dni na wykonanie testu


         */

    }

    public Long placeOrder(Long bookId, int copies) {
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(bookId, copies))
                .build();
        PlaceOrderResponse response = manipulateOrderService.placeOrder(command);
        return response.getRight();
    }

    private Book givenJavaConcurrency(Long avaliable) {
        return bookJpaRepository.save(new Book("Java Concurrency in practice", 2006, new BigDecimal("50.00"), avaliable));
    }

    private Recipient recipient() {
        return Recipient.builder().email("ela@wp.pl").build();
    }


    private Long availableCopiesOfBooks(Book book) {
        return catalogUseCase.findById(book.getId())
                .get()
                .getAvailable();
    }

}