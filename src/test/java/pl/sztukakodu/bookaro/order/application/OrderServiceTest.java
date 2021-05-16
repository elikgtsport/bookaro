package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.OrderItemCommand;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.PlaceOrderCommand;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.PlaceOrderResponse;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.UpdateOrderStatusCommand;
import pl.sztukakodu.bookaro.order.application.port.QueryOrderUseCase;
import pl.sztukakodu.bookaro.order.domain.Delivery;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest//podczas testu uruchomione zostaną tylko te warstwy odpowiedzialne za bazę danych
//@Import({ManipulateOrderService.class})//dodatkowo prosimy o jeszcze jednego beana zeby utworzył instancję
//        //nie stawiamy całego kontekstu Springa żeby było szybciej

//jednak reygnujemy z dopisywania kolejnych beanów i zalezności w nieskończonosc, w tym przypadku
//lepiej jesli postawimy cały kontekst Springa
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)//oznaczamy testy jako brudzące kontekst Springa, wszystkie beany będa tworzone od nowa a baza danych czyszczona do 0

class OrderServiceTest {

    @Autowired
    BookJpaRepository bookJpaRepository;

    @Autowired
    ManipulateOrderService manipulateOrderService;

    @Autowired
    QueryOrderUseCase queryOrderUseCase;

    @Autowired
    CatalogUseCase catalogUseCase;

    @Test
    public void userCanPlaceOrder() {

        Book javaConcurrency = givenJavaConcurrency(50L);
        Book effectiveJava = givenEffectiveJava(50L);

        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(javaConcurrency.getId(), 10))
                .item(new OrderItemCommand(effectiveJava.getId(), 20))
                .build();

        PlaceOrderResponse response = manipulateOrderService.placeOrder(command);

        assertTrue(response.isSuccess());
        assertEquals(40L, availableCopiesOfBooks(javaConcurrency));
        assertEquals(30L, availableCopiesOfBooks(effectiveJava));
    }


    @Test
    public void userCannotRevokePaidOrder() {
        // user nie moze wycofac juz oplaconego zamowienia
        Book javaConcurrency = givenJavaConcurrency(50L);
        String recipient = "ela@wp.pl";
        Long orderId = placeOrder(javaConcurrency.getId(), 15, recipient);

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(orderId, OrderStatus.PAID, user(recipient));
        //pay for order
        manipulateOrderService.updateOrderStatus(command);
        //get paid order status
        OrderStatus paidStatus = queryOrderUseCase.findById(orderId).get().getStatus(); //PAID Status
        UpdateOrderStatusCommand command2 = new UpdateOrderStatusCommand(orderId, OrderStatus.CANCELED, user(recipient));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        {
            //try to cancel paid order
            manipulateOrderService.updateOrderStatus(command2);
        });
        assertTrue(exception.getMessage().contains("Unnable to mark " + paidStatus + " order as: " + OrderStatus.CANCELED));
    }

    @Test
    public void userCannotRevokeShippedOrder() {
        // user nie moze wycofac juz wyslanego zamowienia
        Book javaConcurrency = givenJavaConcurrency(50L);
        String recipient = "ela@wp.pl";
        Long orderId = placeOrder(javaConcurrency.getId(), 15, recipient);

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(orderId, OrderStatus.PAID, user("ela@wp.pl"));
        //pay order
        manipulateOrderService.updateOrderStatus(command);
        UpdateOrderStatusCommand command2 = new UpdateOrderStatusCommand(orderId, OrderStatus.SHIPPED, user(recipient));
        //ship order
        manipulateOrderService.updateOrderStatus(command2);
        //get shipped order status
        OrderStatus shippedStatus = queryOrderUseCase.findById(orderId).get().getStatus(); //SHIPPED Status
        UpdateOrderStatusCommand command3 = new UpdateOrderStatusCommand(orderId, OrderStatus.CANCELED, user(recipient));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        {
            //try to cancel shipped order
            manipulateOrderService.updateOrderStatus(command3);
        });
        assertTrue(exception.getMessage().contains("Unnable to mark " + shippedStatus + " order as: " + OrderStatus.CANCELED));
    }

    @Test
    public void userCannotOrderNoExistingBooks() {
        // user nie moze zamowic nieistniejacych ksiazek

        Book javaConcurrencyInPractice = new Book("Java Concurrency in practice", 2006, new BigDecimal("50.00"), 50L);
        bookJpaRepository.save(javaConcurrencyInPractice);

        // Book notExist = new Book("notExist", 2006, new BigDecimal("50.00"), 50L);
        //Optional<Book> byId = bookJpaRepository.findById(notExist.getId());

        long bookIdToTest = 2L;
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(bookIdToTest, 10))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                manipulateOrderService.placeOrder(command));
        assertTrue(exception.getMessage().contains("Cannot find a book with id: " + bookIdToTest));
    }

    @Test
    public void userCannotOrderNegativeNumberOfBooks() {
        // user nie moze zamowic ujemnej liczby ksiazek

        Book javaConcurrencyInPractice = new Book("Java Concurrency in practice", 2006, new BigDecimal("50.00"), 50L);
        bookJpaRepository.save(javaConcurrencyInPractice);
        int quantityToTest = -2;
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(javaConcurrencyInPractice.getId(), quantityToTest))
                .build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        {
            manipulateOrderService.placeOrder(command);
        });
        assertTrue(exception.getMessage().contains("Can not place order with not positive quantity of books" + quantityToTest));
    }

    @Test
    public void userCanRevokeOrder() {
        Book javaConcurrency = givenJavaConcurrency(50L);
        String recipient = "ela@wp.pl";
        Long orderId = placeOrder(javaConcurrency.getId(), 15, recipient);

        assertEquals(35L, availableCopiesOfBooks(javaConcurrency));
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(orderId, OrderStatus.CANCELED, user(recipient));

        manipulateOrderService.updateOrderStatus(command);
        assertEquals(50L, availableCopiesOfBooks(javaConcurrency));
        OrderStatus status = queryOrderUseCase.findById(orderId).get().getStatus();
        assertEquals(OrderStatus.CANCELED, status);
    }

    @Test
    public void adminCanMarkOrderAsPaid() {
        // given
        Book effectiveJava = givenEffectiveJava(50L);
        String recipient = "marek@example.org";
        Long orderId = placedOrder(effectiveJava.getId(), 15, recipient);
        assertEquals(35L, availableCopiesOf(effectiveJava));

        // when
        String admin = "admin@example.org";
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(orderId, OrderStatus.PAID, adminUser());
        manipulateOrderService.updateOrderStatus(command);
        OrderStatus status = queryOrderUseCase.findById(orderId).get().getStatus();

        // then
        assertEquals(35L, availableCopiesOf(effectiveJava));
        assertEquals(OrderStatus.PAID, status);
    }

    private Recipient recipient() {
        return Recipient.builder().email("recipient@wp.pl").build();
    }


    private Recipient recipient(String email) {
        return Recipient.builder().email(email).build();
    }


    private Book givenJavaConcurrency(Long avaliable) {
        return bookJpaRepository.save(new Book("Java Concurrency in practice", 2006, new BigDecimal("50.00"), avaliable));
    }

    private Book givenEffectiveJava(Long avaliable) {
        return bookJpaRepository.save(new Book("Effective Java", 2005, new BigDecimal("199.00"), avaliable));
    }

    @Test
    public void userCanNotOrderMoreBooksThanAvaliable() {

        Book javaConcurrency = givenJavaConcurrency(5L);

        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient())
                .item(new OrderItemCommand(javaConcurrency.getId(), 10))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        {
            manipulateOrderService.placeOrder(command);
        });
        assertTrue(exception.getMessage().contains("Too many copies of book id: " + javaConcurrency.getId() + " requested: " + 10 + " of: " + javaConcurrency.getAvailable() + " avaliable"));
    }


    @Test
    public void userCannotRevokeOrderOtherUser() {

        Book javaConcurrency = givenJavaConcurrency(50L);
        String adamRecipient = "adam@example.pl";

        Long orderId = placeOrder(javaConcurrency.getId(), 15, adamRecipient); //składamy zamówienie na 15 książek
        assertEquals(35L, availableCopiesOfBooks(javaConcurrency));

        //nie da się anulowac zamówienia z punku widzenia innego usera otherRecipient
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(orderId, OrderStatus.CANCELED,  user("other@example.pl"));

        manipulateOrderService.updateOrderStatus(command);
        assertEquals(35L, availableCopiesOfBooks(javaConcurrency));
        OrderStatus status = queryOrderUseCase.findById(orderId).get().getStatus();
        assertEquals(OrderStatus.NEW, status);//status jest nadal na NEW, bo inny używtkownik nie moze anulować nie swojego zamówienia

    }

    @Test//poprawić w module Security
    public void adminCanRevokeOrderOtherUser() {

        Book javaConcurrency = givenJavaConcurrency(50L);

        String marek = "marek@example.pl";
        String admin = "admin@example.pl";
        Long orderId = placeOrder(javaConcurrency.getId(), 15, marek); //składamy zamówienie na 15 książek
        assertEquals(35L, availableCopiesOfBooks(javaConcurrency));

        //nie da się anulowac zamówienia z punku widzenia innego usera admin
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(orderId, OrderStatus.CANCELED, adminUser());

        manipulateOrderService.updateOrderStatus(command);
        assertEquals(35L, availableCopiesOfBooks(javaConcurrency));
        OrderStatus status = queryOrderUseCase.findById(orderId).get().getStatus();
        assertEquals(OrderStatus.NEW, status);//status jest nadal na NEW, bo inny używtkownik nie moze anulować nie swojego zamówienia

    }

    public Long placeOrder(Long bookId, int copies, String recipient) {
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient(recipient))
                .item(new OrderItemCommand(bookId, copies))
                .build();
        PlaceOrderResponse response = manipulateOrderService.placeOrder(command);
        return response.getRight();
    }

    public Long placeOrder(Long bookId, int copies) {
        return placeOrder(bookId, copies, "example@wp.pl");
    }

    private Long availableCopiesOfBooks(Book book) {
        return catalogUseCase.findById(book.getId())
                .get()
                .getAvailable();
    }

    @Test
    public void shippingCostsAreAddedToTotalOrderPrice() {
        // given
        Book book = givenBook(50L, "49.90");

        // when
        Long orderId = placedOrder(book.getId(), 1);

        // then
        assertEquals("59.80", orderOf(orderId).getFinalPrice().toPlainString());
    }

    @Test
    public void shippingCostsAreDiscountedOver100zlotys() {
        // given
        Book book = givenBook(50L, "49.90");

        // when
        Long orderId = placedOrder(book.getId(), 3);

        // then
        RichOrder order = orderOf(orderId);
        assertEquals("149.70", order.getFinalPrice().toPlainString());
        assertEquals("149.70", order.getOrderPrice().getItemsPrice().toPlainString());
    }

    @Test
    public void cheapestBookIsHalfPricedWhenTotalOver200zlotys() {
        // given
        Book book = givenBook(50L, "49.90");

        // when
        Long orderId = placedOrder(book.getId(), 5);

        // then
        RichOrder order = orderOf(orderId);
        assertEquals("224.55", order.getFinalPrice().toPlainString());
    }

    @Test
    public void cheapestBookIsFreeWhenTotalOver400zlotys() {
        // given
        Book book = givenBook(50L, "49.90");

        // when
        Long orderId = placedOrder(book.getId(), 10);

        // then
        assertEquals("449.10", orderOf(orderId).getFinalPrice().toPlainString());
    }

    private Long placedOrder(Long bookId, int copies, String recipient) {
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(recipient(recipient))
                .item(new OrderItemCommand(bookId, copies))
                .delivery(Delivery.COURIER)
                .build();
        return manipulateOrderService.placeOrder(command).getRight();
    }

    private RichOrder orderOf(Long orderId) {
        return queryOrderUseCase.findById(orderId).get();
    }


    private Book givenBook(long available, String price) {
        return bookJpaRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal(price), available));
    }

    private Long placedOrder(Long bookId, int copies) {
        return placedOrder(bookId, copies, "john@example.org");
    }

    private Book givenJavaConcurrency(long available) {
        return bookJpaRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal("99.90"), available));
    }

    private Book givenEffectiveJava(long available) {
        return bookJpaRepository.save(new Book("Effective Java", 2005, new BigDecimal("199.90"), available));
    }

    private Long availableCopiesOf(Book effectiveJava) {
        return catalogUseCase.findById(effectiveJava.getId())
                .get()
                .getAvailable();
    }

    private User user(String email){
        return new User(email, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));

    }

    private User adminUser(){
        return new User("admin", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}