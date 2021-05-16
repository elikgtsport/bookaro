package pl.sztukakodu.bookaro.catalog.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    @Override
    Optional<Book> findById(Long id);

    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.authors")
        //przez podanie FETCH chcemy aby Hibernate pobrał nam od razu wszystkich autorów
    List<Book> findAllEager();

    @Query(
            " SELECT b FROM Book b JOIN b.authors a " +
                    " WHERE " +
                    " lower(a.name) LIKE lower(concat('%', :name,'%')) "
    )
    List<Book> findByAuthor(@Param("name") String name);

    List<Book> findByAuthors_nameContainsIgnoreCase(String name);

    //alternatywka dla findByAuthor
    //List<Book> findByAuthors_firstNameContainsIgnoreCaseOrAuthors_lastNameContainsIgnoreCase(String firstName, String lastName);

    List<Book> findByTitleStartingWithIgnoreCase(String title);

    Optional<Book> findDistinctFirstByTitle(String title); //zwraca albo jedną wartość albo wcale

    @Query(" SELECT b FROM Book b JOIN b.authors a " +
            " WHERE " +
            " LOWER (b.title) LIKE LOWER(CONCAT ('%', :title,'%'))" +
            " AND LOWER (a.name) LIKE LOWER(CONCAT ('%', :author,'%'))")
    List<Book> findByTitleAndAuthor(@Param("title") String title, @Param("author") String author);

}
