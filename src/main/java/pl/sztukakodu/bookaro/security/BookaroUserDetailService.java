package pl.sztukakodu.bookaro.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.sztukakodu.bookaro.user.db.UserEntityRepository;

@AllArgsConstructor
public class BookaroUserDetailService implements UserDetailsService {

    public final UserEntityRepository userEntiryRepository;
    public final AdminConfig adminConfig;

    //tu nam się przyda Admin Config bo będziemy walidowac
    // czy dany użytkownik który próbuje sie uwierzytelnić jest w bazie danych
    // i czy nie jest administartorem, którego nie mamy w bazie danych a który jest nadrzędny (nasz sytsemAdmin)


    //na podstawie nazwy użytkownika próbujemy go odnaleźć w bazie danych
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(adminConfig.getUsername().equalsIgnoreCase(username)) {
            return adminConfig.adminUser();
        }
        return userEntiryRepository.findByUsernameIgnoreCase(username)
                .map(UserEntityDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
