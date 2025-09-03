package pl.bzowski.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.quarkus.elytron.security.common.BcryptUtil;

@ApplicationScoped
public class UserService {

    @Transactional
    public boolean register(UserRegistrationForm form) {
        // Sprawdź, czy użytkownik już istnieje
        if (RegisteredUser.find("username", form.username).firstResult() != null) {
            return false; // użytkownik istnieje
        }

        // Utwórz nowego użytkownika
        RegisteredUser newUser = new RegisteredUser();
        newUser.username = form.username;
        newUser.email = form.email;
        // Hashuj hasło z bcrypt
        newUser.passwordHash = hashPassword(form.password);
        newUser.role = "USER";
        newUser.persist();
        return true;
    }

    public boolean authenticate(UserLoginForm form) {
        RegisteredUser user = RegisteredUser.find("username", form.username).firstResult();
        if (user == null) {
            return false;
        }
        return checkPassword(form.password, user.passwordHash);
    }

    private String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    private boolean checkPassword(String plainPassword, String hashed) {
        return BcryptUtil.matches(plainPassword, hashed);
    }
}
