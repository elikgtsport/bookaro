package pl.sztukakodu.bookaro.security;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.sztukakodu.bookaro.user.db.UserEntityRepository;

@AllArgsConstructor
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(AdminConfig.class)
@Profile("!test")//definiujemy jak aplikacja ma się zachowac w różnych środowiskach
//profil negatywny do profilu test, nie chcemy uruchamiać tej konfoguracji dla profilu test
    //i jeszcze w testach trzeba włączyć @ActiveProfile("test")
class BookaroSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserEntityRepository userEntityRepository;
    public final AdminConfig adminConfig;

    @Bean
        //jedna instancja Usera w calym systemie
    User systemUser() {
        //return new User("systemUser", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        return adminConfig.adminUser();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/catalog/**", "/uploads/**", "/authors/**").permitAll()
                .mvcMatchers(HttpMethod.POST, "/orders", "/login", "/users").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()//uwierzytelnianie typu basic
                .and()
                //wpinamy się przed UsernamePasswordAuthenticationFilter, wstrzykujemy nasz JsonusernameAuthenticationFilter
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @SneakyThrows
    private JsonUsernameAuthenticationFilter authenticationFilter() {
        JsonUsernameAuthenticationFilter filter = new JsonUsernameAuthenticationFilter();
        filter.setAuthenticationManager(super.authenticationManager());
        return filter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
//                .inMemoryAuthentication()
//                .withUser("marek@example.org")
//                .password("{noop}xxx")//nie używaj encodowania hasła nooperation
//                .roles("USER")
//                .and()
//                .withUser("admin")
//                .password("{noop}xxx")
//                .roles("ADMIN");
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        BookaroUserDetailService detailService = new BookaroUserDetailService(userEntityRepository, adminConfig);
        provider.setUserDetailsService(detailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
