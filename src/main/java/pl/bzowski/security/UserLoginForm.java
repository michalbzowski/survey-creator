package pl.bzowski.security;


import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.FormParam;

public class UserLoginForm {

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @FormParam("username")
    public String username;

    @NotBlank(message = "Hasło jest wymagane")
    @FormParam("password")
    public String password;
}
