package pl.bzowski.team.infrastructure;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.team.Team;
import pl.bzowski.shared.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.team.api.TeamDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class TeamRepository extends RepositoryBase {

    private static final Logger log = LoggerFactory.getLogger(TeamRepository.class);

    @WithTransaction
    public Uni<TeamDTO> createTeam(TeamDTO teamDTO) {
        log.info("Starting creation of team: {}", teamDTO);

        List<UUID> eventIds = teamDTO.events;

        Uni<List<Event>> eventsUni = Uni.createFrom().item(new ArrayList<>());

        for (UUID eventId : eventIds) {
            eventsUni = eventsUni.flatMap(events ->
                    Event.<Event>findById(eventId)
                            .onItem().ifNull().failWith(() -> {
                                log.info("Nie znaleziono wydarzenia o id: {}", eventId);
                                return new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + eventId);
                            })
                            .onItem().invoke(event -> {
                                if (event.team != null) {
                                    log.info("Wydarzenie o id {} jest już przypisany zespół", eventId);
                                    throw new IllegalArgumentException("Wydarzenie jest już przypisany zespół");
                                }
                            })
                            .map(event -> {
                                events.add(event);
                                return events;
                            })
            );
        }

        return eventsUni
                .flatMap(events ->
                        currentRegisteredUserId().flatMap(uuid -> {
                            Team team = new Team();
                            team.name = teamDTO.name;
                            team.events = events;
                            team.registeredUserId = uuid;
                            log.info("Persisting team with name: {}, for user: {}", team.name, uuid);
                            return Panache.withTransaction(team::persist)
                                    .onItem().transform(_ -> {
                                        teamDTO.id = team.id;
                                        for (Event ev : events) {
                                            ev.team = team;
                                        }
                                        return team;
                                    });
                        })
                )
                .onItem()
                .call(team -> {
                    log.info("Team persisted with ID: {}", team.id);
                    return team.persist();
                })
                .onFailure().invoke(failure -> log.info("Failure while creating team: {}", failure.getMessage()))
                .onFailure().recoverWithNull()
                .onItem()
                .transform(Team::toDTO);
    }


    public Uni<List<Team>> listAll() {
        return currentRegisteredUserId().flatMap(uuid -> Team.list("registeredUserId", uuid));
    }

    public Uni<List<Team>> listAllWithEvents() {
        return currentRegisteredUserId().flatMap(userId ->
                Team.<Team>list("registeredUserId", userId)
                        .flatMap(teams -> {
                            // Jawnie dla każdej listy wykonujemy fetch events
                            return Multi.createFrom().iterable(teams)
                                    .onItem().transformToUniAndConcatenate(team ->
                                            Mutiny.fetch(team.events)
                                                    .replaceWith(team)
                                    )
                                    .collect().asList();
                        })
        );
    }

    public Uni<Boolean> teamHasMembers(UUID teamId) {
        return Team.<Team>findById(teamId)
                .onItem()
                .transformToUni(t -> Uni.createFrom().item(!t.members.isEmpty()))
                .replaceIfNullWith(Boolean.FALSE);
    }
}
