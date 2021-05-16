package pl.sztukakodu.bookaro.order.price;

import org.springframework.stereotype.Service;
import pl.sztukakodu.bookaro.order.domain.Order;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceService {

    private final List<DiscountStrategy> strategies = List.of(
            new DeliveryDiscountStrategy(),
            new TotalDiscountStrategy()
    );

    @Transactional
    public OrderPrice calculatePriceOrder(Order order) {
        System.out.println(order + "ggggggggggggggggggggggggggggggggggggggggggggggg");
        BigDecimal itemsPrice = order.getItemsPrice();
        System.out.println("itemsPrice " + " " + itemsPrice);
        System.out.println("order.getDeliveryPrice() " + " " + order.getDeliveryPrice());
        return new OrderPrice(
                itemsPrice,
                order.getDeliveryPrice(),
                discounts(order)
        );
    }

    private BigDecimal discounts(Order order) {
        return strategies
                .stream()
                .map(strategy -> strategy.calculateOrder(order))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
