package pl.sztukakodu.bookaro.order.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.sztukakodu.bookaro.order.application.RichOrder;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.UpdateOrderStatusCommand;
import pl.sztukakodu.bookaro.order.application.port.QueryOrderUseCase;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.security.UserSecurity;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.PlaceOrderCommand;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {
    private final QueryOrderUseCase queryOrderUseCase;
    private final ManipulateOrderUseCase manipulateOrder;
    private final UserSecurity userSecurity;

    @GetMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)//200
    public List<RichOrder> getAllOrders() {
        log.info("Pobieranie wszystkich zamówień [findAll]");
        return queryOrderUseCase.findAll();
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})//poprzeicnku oznacza LUB
    @GetMapping("/{id}")
    public ResponseEntity<RichOrder> getOrderById(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        //wstrzykujemy obiekt User, tu mamy dostęp do uwierzytelnionego użytkownika
        log.info("Pobieranie zamówienia [Get Order By]: " + id);
        return queryOrderUseCase
                .findById(id)
                .map(order -> authorize(order, user))
                .orElse(ResponseEntity.notFound().build());//404
    }

    public ResponseEntity<RichOrder> authorize(RichOrder order, UserDetails user) {
        if (userSecurity.isOwnerOrAdmin(order.getRecipient().getEmail(), user)) {
            return ResponseEntity.ok(order); //200
        }
        return ResponseEntity.status(FORBIDDEN).build(); //403

    }

    @PostMapping
    @ResponseStatus(CREATED)
    public ResponseEntity<Object> createOrder(@RequestBody PlaceOrderCommand command) {
        return manipulateOrder
                .placeOrder(command)
                .handle(
                        orderId -> ResponseEntity.created(orderUri(orderId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @PatchMapping("/{id}/status")
    public ResponseEntity<Object> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails user) {
        String status = body.get("status");
        OrderStatus orderStatus = OrderStatus
                .parseString(status)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unknown status: " + status));
        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(id, orderStatus, user);
        return manipulateOrder.updateOrderStatus(command)
                .handle(
                        newStatus -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.status(error.getStatus()).build()
                );
    }

    URI orderUri(Long orderId) {
        return new CreatedURI("/" + orderId).uri();
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void removeOrder(@PathVariable Long id) {
        log.info("Usuwanie zamówienia [Remove By Id]: " + id);
        manipulateOrder.deleteOrderById(id);
    }

//    @PatchMapping("/{id}/status")
//    @ResponseStatus(HttpStatus.ACCEPTED) //202
//    public void updateOrderStatusById(@PathVariable Long id, @Validated(UpdateValidation.class) @RequestBody RestOrderCommand command) {
//        log.info("Aktualizacja zamówienia [Update Order]: " + id);
//        QueryOrderUseCase.UpdateOrderResponse response = queryOrderUseCase.updateOrderStatus(command.toUpdateOrderCommand(id));
//        if (!response.isSucces()) {
//            String message = String.join(", ", response.getErrors());
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
//        }
//    }


//    @Data
//    public static class RestOrderCommand {
//
//        @NotBlank(message = "Please provide an item", groups = {CreateValidation.class})
//        private List<OrderItem> items;
//
//        @NotBlank(message = "Please provide a recipient", groups = {CreateValidation.class})
//        private Recipient recipient;
//
//        @NotNull(message = "Please provide a status", groups = {CreateValidation.class})
//        private OrderStatus status;
//
//        public PlaceOrderCommand toCreateOrderCommand() {
//            return ManipulateOrderUseCase.PlaceOrderCommand
//                    .builder()
//                    .recipient(recipient)
//                    .items(items)
//                    .build();
//        }
//        public UpdateOrderCommand toUpdateOrderCommand(Long id) {
//            return new UpdateOrderCommand(id, status);
//        }
//    }
}
