package pl.sztukakodu.bookaro.order.price;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class OrderPrice {

    BigDecimal itemsPrice;
    BigDecimal delivieryPrice;
    BigDecimal discounts;

    public BigDecimal finalPrice() {
        return itemsPrice.add(delivieryPrice).subtract(discounts);
    }

}
