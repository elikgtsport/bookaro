package pl.sztukakodu.bookaro.catalog.application.port;

import pl.sztukakodu.bookaro.catalog.domain.Author;

import java.util.List;

public interface AuthorUseCase {

    List<Author> findAll();


}
