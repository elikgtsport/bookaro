package pl.sztukakodu.bookaro.catalog.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.AuthorJpaRepository;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Author;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.uploads.application.port.UploadUseCase;
import pl.sztukakodu.bookaro.uploads.application.port.UploadUseCase.SaveUploadCommand;
import pl.sztukakodu.bookaro.uploads.domain.Upload;

import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
class CatalogService implements CatalogUseCase {

    private final BookJpaRepository repository;
    private final AuthorJpaRepository authorRepository;
    private final UploadUseCase upload;

    @Override
    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<Book> findByTitle(String title) {
        return repository.findByTitleStartingWithIgnoreCase(title);
    }

    @Override
    public List<Book> findByAuthor(String author) {
        return repository.findByAuthor(author);
    }

    @Override
    public List<Book> findByTitleAndAuthor(String title, String author) {
        return repository.findByTitleAndAuthor(title, author);
    }

    @Override
    public Optional<Book> findOneByTitle(String title) {
        return repository.findDistinctFirstByTitle(title);
    }

    @Override
    public List<Book> findAll() {
        return repository.findAllEager();
    }

    @Override
    @Transactional //Contex Hiberante zakładany jest na transakcje, jeśli ich nie zdefiniujemy to będą one na każdą operację,
    //ta anotacja powoduje ze transakcja będzie obejmowała wszystkie metody i to co się dzieje w obrębie tej metody, wydłużamy cykl zycia
    //encji. W momencie wejścia do tej metody otwierany jest Contex Hibernate
    public Book addBook(CreateBookCommand command) {
        Book book = toBook(command);
        return repository.save(book);//to jest tworzona sesja na czas zapisu
    }

    private Book toBook(CreateBookCommand command) {
        Book book = new Book(command.getTitle(), command.getYear(), command.getPrice(), command.getAvailable());
        Set<Author> authors = fetchAuthorsById(command.getAuthors());
        updateBooks(book, authors);
        return book;
    }

    private void updateBooks(Book book, Set<Author> authors) {
        book.clearAuthors();
        authors.forEach(book::addAuthor);
    }

    private Set<Author> fetchAuthorsById(Set<Long> authors) {
        return authors
                .stream()//mapujemy z Seta autorId na instancję Autora (prawdziwego obiektu)
                .map(authorId ->
                        authorRepository
                                .findById(authorId)
                                .orElseThrow(() -> new IllegalArgumentException("Unnable to find author with id: " + authorId))
                )
                .collect(Collectors.toSet());
    }


    @Override
    public void removeById(Long id) {
        if (id != null) {
            repository.deleteById(id);
        }
    }

    @Override
    @Transactional //podczas aktualizowania ksiązki i dodania tej anotacji, to ta ksiązka book jest w stanie zarządzanym
    //zmiany na niej wykonywane zostaną na koniec spersystowane do Hiberante
    public UpdateBookResponse updateBook(UpdateBookCommand command) {
        return repository
                .findById(command.getId())
                .map(book -> {
                    // i wtedy nie musimy extra zapisywać do bazy po zmianach nic
//                    Book updatedBook = updateFields(command, book);
//                    repository.save(updatedBook);
                    updateFields(command, book);
                    return UpdateBookResponse.SUCCESS;
                })
                .orElseGet(() -> new UpdateBookResponse(false, Arrays.asList("Book not found with id: " + command.getId())));
    }

    private Book updateFields(UpdateBookCommand command, Book book) {
        if (command.getTitle() != null) {
            book.setTitle(command.getTitle());
        }
        if (command.getYear() != null) {
            book.setYear(command.getYear());
        }
        if (command.getPrice() != null) {
            book.setPrice(command.getPrice());
        }
        if (command.getAuthors() != null && !command.getAuthors().isEmpty()) {
            Set<Author> authors = fetchAuthorsById(command.getAuthors());
            updateBooks(book, authors);
        }
        return book;
    }

    @Override
    public void updateBookCover(UpdateBookCoverCommand command) {
        repository.findById(command.getId())
                .ifPresent(
                        book -> {
                            Upload savedUpload = upload.save(new SaveUploadCommand(command.getFileName(), command.getFile(), command.getContentType()));
                            book.setCoverId(savedUpload.getId());
                            repository.save(book);
                        });
    }

    @Override
    public void removeBookCoverById(Long id) {
        repository.findById(id)
                .ifPresent(book -> {
                    if (book.getCoverId() != null) {
                        upload.deleteById(book.getCoverId());
                        book.setCoverId(null);
                        repository.save(book);
                    }
                });
    }


}
