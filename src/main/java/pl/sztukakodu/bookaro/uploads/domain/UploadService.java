package pl.sztukakodu.bookaro.uploads.domain;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.sztukakodu.bookaro.uploads.application.port.UploadUseCase;
import pl.sztukakodu.bookaro.uploads.db.UploadJpaRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UploadService implements UploadUseCase {

    private final UploadJpaRepository repository;

    // private final Map<String, Upload> storage = new ConcurrentHashMap<>();

    public Upload save(SaveUploadCommand command) {
        //String newId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        Upload upload = new Upload(
                command.getFileName(),
                command.getFile(),
                command.getContentType());
        repository.save(upload);
        log.info("Upload saved: " + upload.getFileName() + " with id: " + upload.getId());
        return upload;
    }

//    @Override
//    public Optional<Upload> findById(String id) {
//        Upload uploadId = storage.get(id);
//        return Optional.ofNullable(uploadId);
//    }

    @Override
    public Optional<Upload> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
