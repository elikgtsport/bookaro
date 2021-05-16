package pl.sztukakodu.bookaro.users.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sztukakodu.bookaro.users.web.application.port.UserRegistrationUseCase;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@RestController
//controller do rejestracji nowego konta w systemie
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserRegistrationUseCase userRegistrationUseCase;

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegisterCommand commad) {
        return userRegistrationUseCase
                .register(commad.getUsername(), commad.getPassword())
                .handle(
                        //nie możemy zwrócić created bo potrzebne jest URI, a uri jest wtedy gdy mamy odpowiedni endpoint dla użytkowniów
                        entity -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.badRequest().body(error));//przekazujemy błąd w ciele metody
    }

    @Data
    static class RegisterCommand {

        @Email
        String username;

        @Size(min = 3, max = 100)
        String password;

    }

}
