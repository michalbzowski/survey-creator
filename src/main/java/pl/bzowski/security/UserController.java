package pl.bzowski.security;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Path("/sec/")
public class UserController {

  @Inject
  Template register;

  @Inject
  Template login;

  @Inject
  Template loginError;

  @Inject
  UserService userService;

  @GET
  @Path("register")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance showRegisterForm() {
    return register.instance();
  }

  @GET
  @Path("login")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance showLoginForm() {
    return login.instance();
  }

  @GET
  @Path("loginError")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance showLoginError() {
    return loginError.instance();
  }

  @POST
  @Path("register")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
  public Response registerUser(@BeanParam @Valid UserRegistrationForm form) {
    boolean created = userService.register(form);
    if (created) {
      return Response.seeOther(UriBuilder.fromPath("/web/events").build()).build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST)
              .entity("Błąd rejestracji: użytkownik może już istnieć").build();
    }
  }

  @POST
  @Path("logout")
  public Response logout() {
    // Ustaw cookie na wygasłe, aby usunąć sesję
    NewCookie logoutCookie = new NewCookie.Builder("quarkus-credential")
            .maxAge(0)
            .path("/")
            .build();

    return Response.noContent()
            .cookie(logoutCookie)
            .build();
  }
}
