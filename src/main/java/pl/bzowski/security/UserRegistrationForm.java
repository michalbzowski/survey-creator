package pl.bzowski.security;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.FormParam;

public class UserRegistrationForm {

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50)
    @FormParam("username")
    public String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format email")
    @FormParam("email")
    public String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    @FormParam("password")
    public String password;
    
    // Opcjonalnie potwierdzenie hasła
}
