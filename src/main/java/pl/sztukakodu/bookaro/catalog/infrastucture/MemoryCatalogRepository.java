//package pl.sztukakodu.bookaro.catalog.infrastucture;
//
//import lombok.AllArgsConstructor;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Repository;
//import pl.sztukakodu.bookaro.catalog.domain.Book;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Repository
//@AllArgsConstructor
//public class MemoryCatalogRepository {

//        implements CatalogRepository {
//    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
//    private final AtomicLong ID_NEXT_VALUE = new AtomicLong(0L);
//
//
//    @Override
//    public List<Book> findAll() {
//        return new ArrayList<>(storage.values());
//    }
//
//    @Override
//    public Book save(Book book) {
//        if (book.getId() != null) {
//            storage.put(book.getId(), book);
//        } else {
//            long id = nextId();
//            book.setId(id);
//            storage.put(id, book);
//        }
//        return book;
//    }
//
//    @Override
//    public Optional<Book> findById(Long id) {
//        return Optional.ofNullable(storage.get(id));
//    }
//
//    @Override
//    public void deleteById(Long id) {
//        if (id != null) {
//            storage.remove(id);
//        }
//    }
//
//    private long nextId() {
//        return ID_NEXT_VALUE.getAndIncrement();
//    }
//}
