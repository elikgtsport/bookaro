package pl.sztukakodu.bookaro;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@Configuration //ta klasa zostanie zaczytana przy starcie aplikacji, została utworzona poniewaz
//w BookaroApplication jak było @EnableJpaAuditing to robiło szkody w testach
public class JpaConfiguration {
}
