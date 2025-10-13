package pl.bzowski.responses.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pl.bzowski.team.Team;
import pl.bzowski.events.Event;
import pl.bzowski.team.member.TeamMember;
import pl.bzowski.persons.Person;
import pl.bzowski.events.PersonEventAnswer;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/web/responses")
public class ResponsePageResource {

    private static final Logger logger = Logger.getLogger(ResponsePageResource.class.getName());

    private final Template responseForm;
    private final Template thankYou;
    private final Template error;

    public ResponsePageResource(Template responseForm, Template thankYou, Template error) {
        this.responseForm = responseForm;
        this.thankYou = thankYou;
        this.error = error;
    }

    @GET
    @Path("/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> showForm(@PathParam("token") UUID token) {
        return TeamMember.find("linkToken", token)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Nie znaleziono linku"))
                .map(link -> responseForm.data("link", link));
    }


    @POST
    @Path("/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @WithTransaction
    public Uni<TemplateInstance> submitAnswer(@PathParam("token") UUID token, Map<String, String> answers) {
        logger.info(String.format("Submit answer for %s - %d", token.toString(), answers.size()));

        return TeamMember.find("linkToken", token)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Nie znaleziono linku " + token))
                .flatMap(link -> Person.findById(((TeamMember) link).personId)
                        .flatMap(www -> {
                            Person person = (Person) www;
                            Team team = ((TeamMember) link).team;
                            // Przetwarzamy kolejne odpowiedzi sekwencyjnie reaktywnie
                            Uni<Void> allUpdates = Uni.createFrom().voidItem();

                            for (Map.Entry<String, String> entry : answers.entrySet()) {
                                UUID eventId = UUID.fromString(entry.getKey());
                                PersonEventAnswer.Answer answer;
                                try {
                                    answer = PersonEventAnswer.Answer.valueOf(entry.getValue());
                                } catch (IllegalArgumentException e) {
                                    return Uni.createFrom().failure(new IllegalArgumentException("Invalid answer value"));
                                }

                                final PersonEventAnswer.Answer finalAnswer = answer;
                                final UUID finalEventId = eventId;

                                allUpdates = allUpdates
                                        .flatMap(v -> Event.findById(finalEventId)
                                                .flatMap(event -> {
                                                    if (event == null) {
                                                        return Uni.createFrom().voidItem();
                                                    }
                                                    return PersonEventAnswer.find("person = ?1 and team = ?2 and event = ?3", person, team, event)
                                                            .firstResult()
                                                            .flatMap(aaa -> {
                                                                PersonEventAnswer pqa = (PersonEventAnswer) aaa;
                                                                if (pqa != null) {
                                                                    pqa.answer = finalAnswer;
                                                                    return pqa.persistAndFlush().replaceWithVoid();
                                                                } else {
                                                                    PersonEventAnswer newAnswer = new PersonEventAnswer();
                                                                    newAnswer.person = person;
                                                                    newAnswer.team = team;
                                                                    newAnswer.event = (Event) event;
                                                                    newAnswer.answer = finalAnswer;
                                                                    ((TeamMember) link).teamAnswered = Boolean.TRUE;
                                                                    return newAnswer.persistAndFlush().replaceWithVoid();
                                                                }
                                                            });
                                                }));
                            }

                            return allUpdates.map(v -> thankYou.instance());
                        })
                )
                .onFailure(IllegalArgumentException.class).recoverWithItem(() -> error.instance());
    }
}
