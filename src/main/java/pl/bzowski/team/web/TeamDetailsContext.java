package pl.bzowski.team.web;

import pl.bzowski.groups.Group;
import pl.bzowski.persons.Person;
import pl.bzowski.team.Team;

import java.util.List;

public class TeamDetailsContext {
    private Team team;
    private List<TeamEntryWithCommunicationDTO> links;
    private List<Group> groups;
    private List<Person> persons;

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public void setLinks(List<TeamEntryWithCommunicationDTO> dtos) {
        this.links = dtos;
    }

    public List<TeamEntryWithCommunicationDTO> getLinks() {
        return links;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public List<Person> getPersons() {
        return persons;
    }
}
