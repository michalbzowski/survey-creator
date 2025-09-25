package pl.bzowski.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.FormParam;

import java.util.Set;
import java.util.UUID;

public class GroupCreateRequest {
    @NotBlank
    @FormParam("name")
    public String name;

    // przyjmujemy listę UUID osób wybranych w formularzu
    @FormParam("persons")
    public Set<UUID> persons;
}