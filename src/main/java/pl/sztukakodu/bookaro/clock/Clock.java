package pl.sztukakodu.bookaro.clock;

import java.time.Duration;
import java.time.LocalDateTime;

public interface Clock {
    LocalDateTime now();

    class Fake implements Clock {

        private LocalDateTime time;

        public Fake(LocalDateTime time) {
            this.time = time;
        }

        //konstruktor będzie zwracał ciąge jeden punkt w czasie w momencie w którym zostanie wywoiałany ten niżej konstruktor
        public Fake() {
            this(LocalDateTime.now());
        }

        @Override
        public LocalDateTime now() {
            return time;
        }

        //za pomoca tej metody będziemy przesuwać zegar do przodu np o 2h
        public void tick(Duration duration) {
            time = time.plus(duration);
        }
    }
}
