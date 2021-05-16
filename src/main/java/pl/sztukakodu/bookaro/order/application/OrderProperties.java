package pl.sztukakodu.bookaro.order.application;


import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

@Value//(robi prywatne pola i prywatny konstruktor)
@ConstructorBinding //wstrzykiwanie przez konstruktor a nie settery
@ConfigurationProperties("app.orders")//w nawiasach podajemy wartośc kluczy w propertisach
public class OrderProperties {

    String abandonedCron;
    Duration paymentPeriod; //nie robimy Stringa bo nie chce nam się parsować potem do daty
}
