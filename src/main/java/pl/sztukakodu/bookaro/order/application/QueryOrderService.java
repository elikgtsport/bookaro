package pl.sztukakodu.bookaro.order.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.order.application.port.QueryOrderUseCase;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.price.OrderPrice;
import pl.sztukakodu.bookaro.order.price.PriceService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
//pobieranie zamówienia
public class QueryOrderService implements QueryOrderUseCase {

    private final OrderJpaRepository repository;
    // private final OrderRepository repository;
    private final PriceService priceService;
    // private final CatalogRepository catalogRepository;

    //    @Override
//    public List<Order> findAll() {
//        return repository.findAll();
//    }
    @Override
    @Transactional
    public List<RichOrder> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toRichOrder)
                .collect(Collectors.toList());
    }
//
//    @Override
//    public Optional findById(Long id) {
//        return repository.findById(id);
//    }

    @Override
    @Transactional
    public Optional<RichOrder> findById(Long id) {
        return repository
                .findById(id)
                .map(this::toRichOrder);
    }

    private RichOrder toRichOrder(Order order) {
        OrderPrice orderPrice = priceService.calculatePriceOrder(order);
        return new RichOrder(
                order.getId(),
                order.getStatus(),
                order.getItems(),
                order.getRecipient(),
                order.getCreatedAt(),
                orderPrice,
                orderPrice.finalPrice()
        );
    }

    @Override
    public UpdateOrderResponse updateOrderStatus(UpdateOrderCommand command) {
        return repository.findById(command.getId())
                .map(order -> {
                    Order updateOrder = command.updateFields(order);
                    repository.save(updateOrder);
                    return UpdateOrderResponse.SUCCESS;
                })
                .orElseGet(() -> new UpdateOrderResponse(false, Arrays.asList("Book not found with id: " + command.getId())));
    }

    @Override
    public void removeById(Long id) {
        repository.deleteById(id);
    }

}
