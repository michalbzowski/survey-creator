package pl.bzowski.security;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@UserDefinition
public class RegisteredUser extends PanacheEntity {

    @NotBlank
    @Size(min = 3, max = 50)
    @Username
    public String username;

    @NotBlank
    @Email
    public String email;

    @NotBlank
    @Size(min = 8)
    @Password
    public String passwordHash;

    @Roles
    public String role;

    // Możesz dodać role, status, itp.
}
