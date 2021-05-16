package pl.sztukakodu.bookaro.clock;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
//stworzone dla testów w AbandonedOrdersJobTest
public class SystemClock implements Clock {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
