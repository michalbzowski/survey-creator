package pl.bzowski.responses.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pl.bzowski.members.Member;
import pl.bzowski.team.Team;
import pl.bzowski.events.Event;
import pl.bzowski.persons.Person;
import pl.bzowski.answers.Answer;

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
        return Member.find("linkToken", token)
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

        return Member.find("linkToken", token)
                .<Member>firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Nie znaleziono linku " + token))
                .flatMap(member -> {
                            Team team = ((Member) member).team;
                            // Przetwarzamy kolejne odpowiedzi sekwencyjnie reaktywnie
                            Uni<Void> allUpdates = Uni.createFrom().voidItem();

                            for (Map.Entry<String, String> entry : answers.entrySet()) {
                                UUID eventId = UUID.fromString(entry.getKey());
                                Answer.AnswerValue answerValue;
                                try {
                                    answerValue = Answer.AnswerValue.valueOf(entry.getValue());
                                } catch (IllegalArgumentException e) {
                                    return Uni.createFrom().failure(new IllegalArgumentException("Invalid answer value"));
                                }

                                final Answer.AnswerValue finalAnswerValue = answerValue;
                                final UUID finalEventId = eventId;

                                allUpdates = allUpdates
                                        .flatMap(v -> Event.findById(finalEventId)
                                                .flatMap(event -> {
                                                    if (event == null) {
                                                        return Uni.createFrom().voidItem();
                                                    }
                                                    return Answer.find("member = ?1 and team = ?2 and event = ?3", member, team, event)
                                                            .firstResult()
                                                            .flatMap(aaa -> {
                                                                Answer pqa = (Answer) aaa;
                                                                if (pqa != null) {
                                                                    pqa.answerValue = finalAnswerValue;
                                                                    return pqa.persistAndFlush().replaceWithVoid();
                                                                } else {
                                                                    Answer newAnswer = new Answer();
                                                                    newAnswer.member = member;
                                                                    newAnswer.team = team;
                                                                    newAnswer.event = (Event) event;
                                                                    newAnswer.answerValue = finalAnswerValue;
                                                                    ((Member) member).teamAnswered = Boolean.TRUE;
                                                                    return newAnswer.persistAndFlush().replaceWithVoid();
                                                                }
                                                            });
                                                }));
                            }

                            return allUpdates.map(v -> thankYou.instance());
                        });

    }
}
