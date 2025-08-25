package pl.bzowski.responses.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import pl.bzowski.links.PersonSurveyLink;
import pl.bzowski.persons.Person;
import pl.bzowski.question.PersonQuestionAnswer;
import pl.bzowski.question.Question;
import pl.bzowski.surveys.Survey;

import java.util.Map;
import java.util.Optional;
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
    public TemplateInstance showForm(@PathParam("token") UUID token) {
        PersonSurveyLink link = PersonSurveyLink.find("linkToken", token).firstResult();
        if (link == null) {
            throw new NotFoundException("Nie znaleziono linku");
        }

        return responseForm.data("link", link);
    }

    @POST
    @Path("/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public TemplateInstance submitAnswer(@PathParam("token") UUID token, @RequestBody Map<String, String> answers) {
        logger.info(String.format("Submit answer for %s - %d", token.toString(), answers.size()));
        PersonSurveyLink link = PersonSurveyLink.find("linkToken", token).firstResult();
        if (link == null) {
            throw new NotFoundException("Nie znaleziono linku " + link);
        }
        try {
            Person person = link.person;
            Survey survey = link.survey;
            for (String key : answers.keySet()) {
                String value = answers.get(key);
                logger.info(String.format("Key: %s, value: %s", key, value));
                Question question;


                question = Question.findById(UUID.fromString(key));

                logger.info("Question: " + question.title);
                try {
                    Optional<PersonQuestionAnswer> pqa = PersonQuestionAnswer
                            .find("person = ?1 and survey = ?2 and question = ?3 ", person, survey, question)
                            .firstResultOptional();

                    PersonQuestionAnswer.Answer answer = PersonQuestionAnswer.Answer.valueOf(value);
                    if (pqa.isPresent()) {
                        logger.info("PQA present");
                        PersonQuestionAnswer personQuestionAnswer = pqa.get();
                        personQuestionAnswer.answer = answer;
                        logger.info(String.format("Updated %s", personQuestionAnswer.id));
                    } else {
                        logger.info("PQA absent");
                        PersonQuestionAnswer personQuestionAnswer = new PersonQuestionAnswer();
                        personQuestionAnswer.person = person;
                        personQuestionAnswer.survey = survey;
                        link.surveyAnswered = Boolean.TRUE;
                        personQuestionAnswer.question = question;
                        personQuestionAnswer.answer = answer;
                        personQuestionAnswer.persist();
                        logger.info(String.format("Persisted new %s", personQuestionAnswer.id));
                    }
                } catch (RuntimeException ex) {
                    logger.info("Exception:" + ex.getMessage());
                    throw new RuntimeException("lol");

                }

            }
            return thankYou.instance();
        } catch (IllegalArgumentException e) {
            return error.instance();
        }
    }
}
