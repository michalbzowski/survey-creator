package pl.bzowski.persons;

import java.util.UUID;

public record PersonBasicInfoDto(
        UUID id,
        String firstName,
        String lastName,
        String email) {
}
