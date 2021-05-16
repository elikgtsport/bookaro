package pl.sztukakodu.bookaro.uploads.application.port;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.sztukakodu.bookaro.uploads.domain.Upload;

import java.util.Optional;

public interface UploadUseCase {

    Upload save(SaveUploadCommand command);

    Optional<Upload> findById(Long id);

    void deleteById(Long id);

    @Value
    @AllArgsConstructor
    class SaveUploadCommand {
        String fileName;
        byte[] file;
        String contentType;


    }
}
