package pl.sztukakodu.bookaro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase//chcemy żeby dane były pobierane z bazy testowej typu H2
class BookaroApplicationTests {

	@Test
	void contextLoads() {
	}

}
