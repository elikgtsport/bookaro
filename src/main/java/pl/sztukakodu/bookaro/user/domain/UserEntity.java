package pl.sztukakodu.bookaro.user.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    private String username;

    private String password;

    @CollectionTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"))
    //nazwa tabeli łącznikowej, nazwa kolumny dla user
    @ElementCollection(fetch = FetchType.EAGER)//pojedyncze Stringi a nie obiekty więc moze byc Eager
    //prosta tabel łącząca userId z rolami zamiast OneToMany
    @Column(name = "role") //nazwa kolumny dla roli
    private Set<String> roles = new HashSet<>();
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = Set.of("ROLE_USER");
    }
}
