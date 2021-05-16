package pl.sztukakodu.bookaro.order.application;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.sztukakodu.bookaro.order.domain.OrderItem;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;
import pl.sztukakodu.bookaro.order.price.OrderPrice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Value
@AllArgsConstructor
public class RichOrder {
    Long id;
    OrderStatus status;
    Set<OrderItem> items;
    Recipient recipient;
    LocalDateTime createdAt;
    OrderPrice orderPrice;
    BigDecimal finalPrice;

}
