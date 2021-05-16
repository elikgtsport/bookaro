package pl.sztukakodu.bookaro.uploads.web;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sztukakodu.bookaro.uploads.application.port.UploadUseCase;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/uploads")
@AllArgsConstructor
public class UploadsController {

    private final UploadUseCase uploadUseCase;


    @GetMapping("/{id}")
    public ResponseEntity<UploadResponse> getUpload(@PathVariable Long id) {
        return uploadUseCase.findById(id)
                .map(
                        uploadFile -> {
                            UploadResponse response = new UploadResponse(
                                    uploadFile.getId(), uploadFile.getContentType(), uploadFile.getFileName(), uploadFile.getCreatedAt());
                            return ResponseEntity.ok(response);
                        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getUploadFile(@PathVariable Long id) {
        return uploadUseCase.findById(id)
                .map(file -> {
                            String contentDisposition = "attachment; fileName= \"" + file.getFileName() + "\"";
                            byte[] bytes = file.getFile();
                            Resource resource = new ByteArrayResource(bytes);
                            return ResponseEntity
                                    .ok()
                                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                                    .contentType(MediaType.parseMediaType(file.getContentType()))
                                    .body(resource);
                        }).orElse(ResponseEntity.notFound().build());
    }

    @Value
    @AllArgsConstructor
    class UploadResponse {

        Long id;
        String contentType;
        String fileName;
        LocalDateTime createdAt;


    }


}
