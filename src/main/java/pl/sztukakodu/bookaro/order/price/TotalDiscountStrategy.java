package pl.sztukakodu.bookaro.order.price;

import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class TotalDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateOrder(Order order) {
        if (isGreaterOrEqual(order, "400.00")) {
            //cheapest book for free
            return lowestBookPrice(order.getItems());//obnizka za przesyłkę
        } else if (isGreaterOrEqual(order, "200.00")) {
            //cheapest book half price
            return lowestBookPrice(order.getItems()).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal lowestBookPrice(Set<OrderItem> items) {
        return items
                .stream()
                .map(book -> book.getBook().getPrice())
                .sorted()
                .findFirst()
                .orElse(BigDecimal.ZERO);


    }

    private boolean isGreaterOrEqual(Order order, String value) {
        return order.getItemsPrice().compareTo(new BigDecimal(value)) >= 0;
    }
}
