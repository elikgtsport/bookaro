package pl.sztukakodu.bookaro.order.domain;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {
    //maszyna stanów - wzorzec projektowy
    NEW{
        @Override
        public UpdateStatusResult updateStatus(OrderStatus orderStatus) {
            switch (orderStatus) {
                case PAID:
                    return UpdateStatusResult.ok(PAID); //nie trzeba zwracać ksiązek do systemu
                case CANCELED:
                    return UpdateStatusResult.revoked(CANCELED); //chcemy żeby ksiązki były przywrócone
                case ABANDONED:
                    return UpdateStatusResult.revoked(ABANDONED); //chcemy żeby ksiązki były przywrócone
                default:
                    return super.updateStatus(orderStatus);
            }
        }
    },
    PAID{
        @Override
        public UpdateStatusResult updateStatus(OrderStatus orderStatus) {
            if (orderStatus == SHIPPED) {
                return UpdateStatusResult.ok(SHIPPED);
            }
            return super.updateStatus(orderStatus);
        }
    },
    CANCELED,
    ABANDONED,//porzucone
    SHIPPED{//wysłane
        @Override
        public UpdateStatusResult updateStatus(OrderStatus orderStatus) {
            return super.updateStatus(orderStatus);
        }
    };

    public static Optional<OrderStatus> parseString(String value) {
        return Arrays.stream(values())
                .filter(it -> StringUtils.equalsIgnoreCase(it.name(), value))
                .findFirst();
    }

    public UpdateStatusResult updateStatus(OrderStatus orderStatus) {
        throw new IllegalArgumentException("Unnable to mark " + this.name() + " order as: " + orderStatus.name());
    }

}
