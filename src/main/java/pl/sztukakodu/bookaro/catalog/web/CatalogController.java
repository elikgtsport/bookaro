package pl.sztukakodu.bookaro.catalog.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.domain.Book;

import javax.validation.constraints.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase.*;


@RequestMapping("/catalog")
@RestController
@AllArgsConstructor
@Slf4j
public class CatalogController {
    private final CatalogUseCase catalogUseCase;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Book> getAll(
            @RequestParam Optional<String> title,
            @RequestParam Optional<String> author) {
        if (title.isPresent() && author.isPresent()) {
            log.info("Pobieranie książek wg tytułu i autora [findByTitleAndAuthor]: title: " + title.get() + ", author: " + author.get());
            return catalogUseCase.findByTitleAndAuthor(title.get(), author.get());
        } else if (title.isPresent()) {
            log.info("Pobieranie książek wg tytułu [findByTitle]: title: " + title.get());
            return catalogUseCase.findByTitle(title.get());
        } else if (author.isPresent()) {
            log.info("Pobieranie książek wg tytułu [findByAuthor]: author: " + author.get());
            return catalogUseCase.findByAuthor(author.get());
        }
        log.info("Pobieranie wszystkich książek [Find All]");
        return catalogUseCase.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable Long id) {
        log.info("Pobieranie książki po id [finfById]: " + id);
        return catalogUseCase
                .findById(id)
                .map(ResponseEntity::ok)
                //.orElse(ResponseEntity.badRequest().build());
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> addBook(@Validated(CreateValidation.class) @RequestBody RestBookCommand command) {
        Book book = catalogUseCase.addBook(command.toCreateCommand());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + book.getId().toString()).build().toUri();
        return ResponseEntity.created(uri).build();
    }

    @PatchMapping("/{id}")
    @Secured({"ROLE_ADMIN"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateBook(@PathVariable Long id, @Validated(UpdateValidation.class) @RequestBody RestBookCommand command) {
        UpdateBookResponse response = catalogUseCase.updateBook(command.toUpdateCommand(id));
        if (!response.isSucces()) {
            String message = String.join(", ", response.getErrors());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @PutMapping(value = "/{id}/cover", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Secured({"ROLE_ADMIN"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addBookCover(@PathVariable Long id, @RequestParam("File") MultipartFile file) throws IOException {
        log.info("Got a file: " + file.getOriginalFilename());
        catalogUseCase.updateBookCover(new UpdateBookCoverCommand(id, file.getBytes(), file.getContentType(), file.getOriginalFilename()));
    }

    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        catalogUseCase.removeById(id);
    }

    @DeleteMapping("/{id}/cover")
    @Secured({"ROLE_ADMIN"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookCover(@PathVariable Long id) {
        catalogUseCase.removeBookCoverById(id);
    }

    @Data
    private static class RestBookCommand {

        @NotBlank(message = "Please provide a title", groups = {CreateValidation.class})
        private String title;

        //@NotBlank(message = "Please provide an author", groups = {CreateValidation.class})
        //private String author;
        @NotEmpty
        private Set<Long> authors; //id-ki autorów


        @NotNull(message = "Year can not be a null", groups = {CreateValidation.class})
        private Integer year;

        @NotNull(message = "Year can not be a null", groups = {CreateValidation.class})
        @DecimalMin(value = "0.00", groups = {CreateValidation.class, UpdateValidation.class})
        private BigDecimal price;

        @NotNull
        @PositiveOrZero
        private Long avaliable;

        public CreateBookCommand toCreateCommand() {
            return new CreateBookCommand(title, authors, year, price, avaliable);
        }

        public UpdateBookCommand toUpdateCommand(Long id) {
            return new UpdateBookCommand(id, title, authors, year, price);
        }
    }
}
