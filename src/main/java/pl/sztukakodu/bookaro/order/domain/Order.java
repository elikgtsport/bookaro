package pl.sztukakodu.bookaro.order.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order extends BaseEntity {

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    //cascade=ALL jest ok, bo podczas usuwania zamówienia chcemy usuwać OrderItem, stosujemy gdy cykl życia jednej encji
    //jest zależny od innej
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")// żeby nie była tworzona trzecia tabela (orders_items), tylko tworzymy nową kolumnę
    @Singular
    private Set<OrderItem> items;

    //usuwając zamówienie nie chcemy usuwać Recipienta, nie może być ALL
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//fetchType=EAGER dla jednego zamówienia pobieramy jeden obiekt recipienta jest domyślny w Many to One
    //transient - wstrzymujemy serializację obiektu
    private Recipient recipient;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Delivery delivery = Delivery.COURIER;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    //przydatne przy debug
    private LocalDateTime updatedAt;

    public UpdateStatusResult updateStatus(OrderStatus newStatus) {
        //zwrócimy tutaj klasę z informacją o aktualnym statusie ksiązki
        UpdateStatusResult result = this.status.updateStatus(newStatus);
        //ptzypisujemy nowy status a zwracamy cały result
        this.status = result.getNewStatus();
        return result;
    }

    public BigDecimal getItemsPrice() {
        return items.stream()
                .map(item -> item.getBook().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDeliveryPrice() {
        System.out.println("ssssssssssssssssss");
        if(items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        System.out.println("DDDDDDDDDDDDDDDDDDD");
        System.out.println(items.size());
        items.forEach(System.out::println);
        return delivery.getPrice();
        //TODO
        //return new BigDecimal("20.00");
    }
}
