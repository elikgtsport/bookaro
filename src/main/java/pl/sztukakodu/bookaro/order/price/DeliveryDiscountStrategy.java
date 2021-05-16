package pl.sztukakodu.bookaro.order.price;

import pl.sztukakodu.bookaro.order.domain.Order;

import java.math.BigDecimal;

public class DeliveryDiscountStrategy implements DiscountStrategy {

    public static final BigDecimal THRESHOLD = BigDecimal.valueOf(100);

    @Override
    public BigDecimal calculateOrder(Order order) {
        if(order.getItemsPrice().compareTo(THRESHOLD) >= 0) {
            System.out.println("Cena " + order.getDeliveryPrice());
            return order.getDeliveryPrice();
        }
        return BigDecimal.ZERO;
    }
}
