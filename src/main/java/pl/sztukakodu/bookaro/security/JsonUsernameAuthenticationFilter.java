package pl.sztukakodu.bookaro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class JsonUsernameAuthenticationFilter extends UsernamePasswordAuthenticationFilter  {

    //tworzymy filtr, który w momencie gdy przyjdzie request to będzie próbował przetworzyć ten request,
    //sparsować request na klasę LoginCommand, wyciągnąć z tej klasy nazwę i hasło użytkownika
    //i spróbowac zautentykować wobec kont użytkowników którzy znajdują się w systemie

    //tutaj definiujemy jak przetwarzamy żądanie hhtp i jak próbujemy się zalogować jako dany użytkownik
    private final ObjectMapper objectMapper = new ObjectMapper();//mapuje Stringi np z Jesonów na klasy javove

    @SneakyThrows//getReader() rzuca wyjątek ale nic sensownego nic z nim nie robi
    //SneakyThrows amienia wyjatek rzucany tutaj w wyjątej Runtime-owy
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //potrzebujemy obiekt który będzie mapował nam Stringa na jakąś klasę Javovą ten obiekt to LoginCommand
        LoginCommand command = objectMapper.readValue(request.getReader(), LoginCommand.class);
        //reader czyta ciało żądania czyli {"username":"", "password:""} z postmana
        //trzeba zautentykowac użytkownika z configure(AuthenticationManagerBuilder auth)
        // i on bedzie zautoryzowany wobec kont zidentyfikowanych w systemie
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                command.getUsername(),
                command.getPassword()
        );
        return this.getAuthenticationManager().authenticate(token);
    }
}
