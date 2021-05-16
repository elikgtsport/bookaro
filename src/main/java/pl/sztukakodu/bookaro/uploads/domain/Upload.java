package pl.sztukakodu.bookaro.uploads.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Upload extends BaseEntity {

    private String fileName;

    private byte[] file;

    @Column(length = 2000)
    private String contentType;

    @CreatedDate
    private LocalDateTime createdAt;

    public Upload(String fileName, byte[] file, String contentType) {
        this.fileName = fileName;
        this.file = file;
        this.contentType = contentType;
    }
}
