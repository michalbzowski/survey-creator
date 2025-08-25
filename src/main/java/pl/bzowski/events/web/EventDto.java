package pl.bzowski.events.web;

import jakarta.ws.rs.FormParam;

import java.time.LocalDateTime;

public class EventDto {
    @FormParam("name")
    public String name;

    @FormParam("location")
    public String location;

    @FormParam("localDateTime")
    public LocalDateTime localDateTime;

    @FormParam("description")
    public String description;
}
