package pl.sztukakodu.bookaro.order.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.clock.Clock;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.UpdateOrderStatusCommand;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

//klasa będzie cyklicznie sprawdzała cyklicznie czy zadanie jest porzucone (nie opłacone)
@Component
@AllArgsConstructor
@Slf4j
public class AbandonedOrdersJob {
    private final OrderJpaRepository orderJpaRepository;
    private final ManipulateOrderUseCase orderUseCase;
    private final OrderProperties properties;
    private final Clock clock;
    private final User systemUser;

    //odpalać cyklicznie w ramach jakiegoś zadania
    @Scheduled(cron = "${app.orders.abandoned-cron}") //co kazde 60 sekund wykonujemy zadanie
    @Transactional
    //między odebraniem zamówień z listy a aktualizacją może dojść do współbieznych żądań, musi się wykonać całość, albo nic
    public void run() {
        Duration paymentPeriod = properties.getPaymentPeriod();

        // w prawdziwym życiu byłoby minusDays(5); zamist LocaldateTime wołamy clock
        //LocalDateTime olderThan = LocalDateTime.now().minus(paymentPeriod);
        LocalDateTime olderThan = clock.now().minus(paymentPeriod);
        log.info("olderThan: " + olderThan);
        //find Abandoned Orders
        List<Order> orders = orderJpaRepository.findByStatusAndCreatedAtLessThanEqual(OrderStatus.NEW, olderThan);
        log.info("Find Orders Abandoned " + orders.size());
        //update status abandoned
        orders.forEach(order -> {
            //tu nie trzeba walidować użytkownika tworzymy sztucznego systemUser
            UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(order.getId(), OrderStatus.ABANDONED, systemUser);
            orderUseCase.updateOrderStatus(command);
        });
    }


}
