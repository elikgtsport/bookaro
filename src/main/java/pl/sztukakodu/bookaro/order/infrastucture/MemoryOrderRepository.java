//package pl.sztukakodu.bookaro.order.infrastucture;
//
//import org.springframework.stereotype.Repository;
//import pl.sztukakodu.bookaro.order.domain.Order;
//import pl.sztukakodu.bookaro.order.domain.OrderRepository;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Repository
//public class MemoryOrderRepository implements OrderRepository {
//
//    private final Map<Long, Order> storage = new ConcurrentHashMap<>();
//    private final AtomicLong ID_NEXT_VALUE = new AtomicLong(0L);
//
//    @Override
//    public Order save(Order order) {
//        if (order.getId() != null) {
//            storage.put(order.getId(), order);
//        } else {
//            long id = nextId();
//            order.setId(id);
//            order.setCreatedAt(LocalDateTime.now());
//            storage.put(id, order);
//        }
//        return order;
//    }
//
//    @Override
//    public List<Order> findAll() {
//        return new ArrayList<>(storage.values());
//    }
//
//    @Override
//    public Optional<Order> findById(Long id) {
//        return Optional.ofNullable(storage.get(id));
//    }
//
//    @Override
//    public void deleteById(Long id) {
//        storage.remove(id);
//    }
//
//    private long nextId() {
//        return ID_NEXT_VALUE.incrementAndGet();
//    }
//}
//
//
