package pl.sztukakodu.bookaro.order.application.port;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import pl.sztukakodu.bookaro.order.application.RichOrder;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface QueryOrderUseCase {

    List<RichOrder> findAll();

    //Optional<Order> findById(Long id);

    Optional<RichOrder> findById(Long id);

    UpdateOrderResponse updateOrderStatus(UpdateOrderCommand command);

    void removeById(Long id);

    @Value
    @Builder
    @AllArgsConstructor
    class UpdateOrderCommand {

        Long id;
        OrderStatus status;

        public Order updateFields(Order order) {
            if (status != null) {
                order.setStatus(status);
            }
            return order;
        }
    }

    @Value
    class UpdateOrderResponse {
        public static UpdateOrderResponse SUCCESS = new UpdateOrderResponse(true, Collections.emptyList());
        boolean succes;
        List<String> errors;
    }
}
