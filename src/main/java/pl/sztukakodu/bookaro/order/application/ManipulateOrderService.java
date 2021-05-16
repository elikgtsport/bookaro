package pl.sztukakodu.bookaro.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.db.RecipientJpaRepository;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderItem;
import pl.sztukakodu.bookaro.order.domain.Recipient;
import pl.sztukakodu.bookaro.order.domain.UpdateStatusResult;
import pl.sztukakodu.bookaro.security.UserSecurity;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional//wiele osób na raz może korzystać z systemu i modyfikowac zamówienie
@RequiredArgsConstructor
public class ManipulateOrderService implements ManipulateOrderUseCase {

    private final CatalogUseCase catalogUseCase;
    private final OrderJpaRepository orderRepository;
    private final BookJpaRepository bookRepository;
    private final RecipientJpaRepository recipientRepository;
    private final UserSecurity userSecurity;

    @Override
    //jesteśmy w trakcie transakcji dlatego możemy tu wykonać aktualizację ksiązek
    public PlaceOrderResponse placeOrder(PlaceOrderCommand command) {
        Set<OrderItem> items = command.getItems()
                .stream()
                .map(this::toOrderItem)
                .collect(Collectors.toSet());
        Order order = Order
                .builder()
                //.recipient(command.getRecipient())
                .recipient(getOrCreateRecipient(command.getRecipient())) //zamiast wkładać całego recipienta, bierzemy go z bazy danych albo
                //zwracamy nową instancję której jeszcze w bazie nie ma. Musimy mieć recipientRepository żeby z bazą danych rozmawiać
                .delivery(command.getDelivery())
                .items(items)
                .build();

        Order save = orderRepository.save(order);
        bookRepository.saveAll(reduceBooks(items));
        return PlaceOrderResponse.success(save.getId());
    }

    //pobieramy recipienta z bazy jesli nie znajdziemy to wkładamy całego, nowego
    private Recipient getOrCreateRecipient(Recipient recipient) {
        Optional<Recipient> byEmailIgnoreCase = recipientRepository.findByEmailIgnoreCase(recipient.getEmail());
        return byEmailIgnoreCase.orElse(recipient);
    }

    private Set<Book> reduceBooks(Set<OrderItem> items) {
        return items
                .stream()
                .map(item -> {
                    Book book = item.getBook();
                    book.setAvailable(book.getAvailable() - item.getQuantity());
                    return book;
                })
                .collect(Collectors.toSet());
    }

    private OrderItem toOrderItem(OrderItemCommand command) {
        isExistBook(command);
        isPositiveNumberQuantityOrder(command.getQuantity());
        Book book = bookRepository.getOne(command.getBookId());
        int quantity = command.getQuantity();
        if (book.getAvailable() >= quantity) {
            return new OrderItem(book, quantity);
        } else {
            throw new IllegalArgumentException("Too many copies of book id: " + book.getId() + " requested: " + quantity + " of: " + book.getAvailable() + " avaliable");
        }
    }

    private void isExistBook(OrderItemCommand command) {
        //nie wiem czy tu nie ma być bookJpaRepository.findById(command.getBookId())??????
        Optional<Book> byId = catalogUseCase.findById(command.getBookId());
        if (!byId.isPresent())
            throw new IllegalArgumentException("Cannot find a book with id: " + command.getBookId());
    }

    private void isPositiveNumberQuantityOrder(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Can not place order with not positive quantity of books" + quantity);
        }
    }

    @Override
    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional //bo jest .save wywoływane
    public UpdateStatusResponse updateOrderStatus(UpdateOrderStatusCommand command) {
        return orderRepository
                .findById(command.getOrderId())
                .map(order -> {
                    if(userSecurity.isOwnerOrAdmin(order.getRecipient().getEmail(), command.getUser())) {
//                    if (!hasAccess(command, order)) {
//                        return UpdateStatusResponse.failure("Unauthorized");
//                    }
                        UpdateStatusResult result = order.updateStatus(command.getStatus());
                        if (result.isRevoked()) {
                            Set<Book> books = revokeBooks(order.getItems());
                            bookRepository.saveAll(books);
                        }
                        //musimy tutaj zwalidować jaki jest status zamówienia i jak zostało porzucone
                        //lub znulowane to nalezy zwrócić te książki do systemu
                        orderRepository.save(order);
                        return UpdateStatusResponse.success(order.getStatus());
                    }
                    return UpdateStatusResponse.failure(Error.FORBIDDEN);
                })
                .orElse(UpdateStatusResponse.failure(Error.NOT_FOUND));
    }

//    private boolean hasAccess(UpdateOrderStatusCommand command, Order order) {
//        String email = command.getEmail();
//        String adminEmail = "admin@wp.pl";
//        return email.equalsIgnoreCase(order.getRecipient().getEmail()) || email.equalsIgnoreCase(adminEmail);
//    }

    //twotzymy zbiór książek które należy zaktualizować z odświeżoną zawartością tych książek
    private Set<Book> revokeBooks(Set<OrderItem> items) {
        return items
                .stream()
                .map(item -> {
                    Book book = item.getBook();
                    book.setAvailable(book.getAvailable() + item.getQuantity());
                    return book;
                })
                .collect(Collectors.toSet());
    }
}
