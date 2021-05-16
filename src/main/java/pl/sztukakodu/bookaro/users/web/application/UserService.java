package pl.sztukakodu.bookaro.users.web.application;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.user.db.UserEntityRepository;
import pl.sztukakodu.bookaro.user.domain.UserEntity;
import pl.sztukakodu.bookaro.users.web.application.port.UserRegistrationUseCase;
@Service
@AllArgsConstructor
public class UserService implements UserRegistrationUseCase {

    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegisterResponse register(String username, String password) {
        if (userEntityRepository.findByUsernameIgnoreCase(username).isPresent()) {
            //failure
            return RegisterResponse.failure("Account already exist");
        }
        //to będzie użytkowik
        UserEntity userEntity = new UserEntity(username, passwordEncoder.encode(password));
       //jesli nie ma takiego uzytkownika, zapisz do bazy danych, zwracamy zapisaną do bazy danych encję
        return RegisterResponse.success(userEntityRepository.save(userEntity));
    }

}
