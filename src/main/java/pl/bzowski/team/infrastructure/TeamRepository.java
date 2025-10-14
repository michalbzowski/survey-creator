package pl.bzowski.team.infrastructure;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.hibernate.reactive.mutiny.Mutiny;
import pl.bzowski.team.Team;
import pl.bzowski.shared.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.team.api.TeamDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class TeamRepository extends RepositoryBase {

    @WithTransaction
    public Uni<TeamDTO> createTeam(TeamDTO teamDTO) {
        List<UUID> eventIds = teamDTO.events;

        // Start with an initial Uni emitting an empty list of Event
        Uni<List<Event>> eventsUni = Uni.createFrom().item(new ArrayList<>());

        // Sequentially process event IDs one by one, accumulating validated Event instances
        for (UUID eventId : eventIds) {
            eventsUni = eventsUni.flatMap(events ->
                    Event.<Event>findById(eventId)
                            .onItem().ifNull().failWith(() -> new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + eventId))
                            .onItem().invoke(event -> {
                                if (event.team != null) {
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
                .onFailure().recoverWithNull()
                .onItem().transform(Team::toDTO);
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

}
