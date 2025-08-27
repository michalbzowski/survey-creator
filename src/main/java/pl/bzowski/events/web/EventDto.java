package pl.bzowski.events.web;

import jakarta.ws.rs.FormParam;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventDto {

    @FormParam("id")
    public UUID id;

    @FormParam("name")
    public String name;

    @FormParam("location")
    public String location;

    @FormParam("localDateTime")
    public LocalDateTime localDateTime;

    @FormParam("description")
    public String description;

    public EventDto() {
    }

    public EventDto(UUID id, String name, String location, LocalDateTime localDateTime, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.localDateTime = localDateTime;
        this.description = description;
    }
}
