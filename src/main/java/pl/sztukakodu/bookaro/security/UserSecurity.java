package pl.sztukakodu.bookaro.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    public boolean isOwnerOrAdmin(String objectOwner, UserDetails user) {
        return isAdmin(user) || isOwner(objectOwner, user);
    }

    private boolean isAdmin(UserDetails user) {
        return user.getAuthorities()
                .stream()
                .anyMatch(u -> u.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));

    }

    private boolean isOwner(String objectOwner, UserDetails user) {
        return user.getUsername().equalsIgnoreCase(objectOwner);
    }
}
