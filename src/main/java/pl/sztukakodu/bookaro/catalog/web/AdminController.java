package pl.sztukakodu.bookaro.catalog.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogInitializerUseCase;

import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
@Slf4j
@Secured({"ROLE_ADMIN"})
public class AdminController {

    private final CatalogInitializerUseCase initializer;

    @PostMapping("/initialization")
    @Transactional
    public void run(String... args) throws Exception {
        initializer.initialize();
    }
}

