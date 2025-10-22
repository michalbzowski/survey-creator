package pl.bzowski.drivers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pl.bzowski.utils.Driver;
import pl.bzowski.utils.Params;

import java.util.Arrays;

@Singleton
public class RegisteredUser {

    @Inject
    private Driver driver;

    public void openSystem() {
        driver.openSystem();
    }

    public void lookAtList(String listName) {
        driver.lookAtList(listName);
    }

    public void askToCreateNew() {
        driver.askToCreateNew();
    }

    public void fillNewTagDetails(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "Dyrygent");

        driver.fillNewTagDetails(name);
    }

    public void confirmNewTag() {
        driver.submitNewTag();
    }

    public void exit() {
        driver.exit();
    }

    public void deleteTag(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "");

        driver.deleteTag(name);
    }

    public void fillNewPersonDetails(String... args) {
        Params params = new Params(args);
        String firstName = params.Optional("firstName", "Default");
        String lastName = params.Optional("lastName", "Default");
        String email = params.Optional("email", "d@d.pl");

        String defaultTag = params.Optional("defaultTag", null);
        String groups = params.Optional("groups", null);

        driver.fillNewPersonDetails(firstName, lastName, email, defaultTag, groups);
    }

    public void confirmNewPerson() {
        driver.confirmNewPerson();
    }

    public void fillGroupFormFields(String... args) {
        Params params = new Params(args);
        String groupName = params.Optional("groupName", "Default");

        driver.fillFormFields("groupName", groupName);

        int checkboxCount = Integer.parseInt(params.Optional("checkboxCount", "0"));
        for (int i = 1; i <= checkboxCount; i++) {
            String checkboxId = params.Optional("checkboxId" + i, null);
            Boolean checkboxValue = Boolean.valueOf(params.Optional("checkboxValue" + 1, null));
            if (checkboxId != null) {
                driver.check(checkboxId, checkboxValue);
            }
        }
    }

    public void fillEventFormFields(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "");
        String location = params.Optional("location", "");
        String datetimeInput = params.Optional("datetimeInput", "");
        String description = params.Optional("description", "");

        driver.fillFormFields("name", name);
        driver.fillFormFields("location", location);
        driver.fillFormValue("datetimeInput", datetimeInput);
        driver.fillFormFields("description", description);

        int checkboxCount = Integer.parseInt(params.Optional("checkboxCount", "0"));
        for (int i = 1; i <= checkboxCount; i++) {
            String checkboxId = params.Optional("checkboxId" + i, null);
            Boolean checkboxValue = Boolean.valueOf(params.Optional("checkboxValue" + 1, null));
            if (checkboxId != null) {
                driver.check(checkboxId, checkboxValue);
            }
        }

        String radioId = params.Optional("clickRadioId", null);
        if (radioId != null) {
            driver.select(radioId);
        }
    }

    public void confirm(String formName) {
        driver.confirm(formName);
    }

    public void edit(String rowData) {
        driver.edit(rowData);
    }

    //ASSERTIONS:
    public void assertNewGroupCreated(String... args) {
        Params params = new Params(args);
        String name = params.Optional("groupName", "");

        driver.assertExistsOnList(name);
    }

    public void assertNewPersonCreated(String... args) {
        Params params = new Params(args);
        String firstName = params.Optional("firstName", "Default");
        driver.assertNewPersonCreated(firstName);
    }

    public void assertTagNotExists(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "");

        driver.assertTagNotExists(name);
    }

    public void assertTagExists(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "Dyrygent");

        driver.assertTagExists(name);
    }

    public void assertIsChecked(String... checkboxes) {
        Params params = new Params(checkboxes);
        String checkboxesIds = params.Optional("checkboxes", "[]");
        Arrays.stream(checkboxesIds.substring(1, checkboxesIds.length() - 1).split(",")).toList()
                .forEach(s -> {
                    String id = s.trim();
                    driver.isChecked(id);
                });
    }

    public void assertNewEventCreated(String... args) {
        Params params = new Params(args);
        String eventName = params.Optional("eventName", "");

        driver.assertNewEventCreated(eventName);
    }

    public void lookAtDetails(String listName, String... args) {
        Params params = new Params(args);
        String rowName = params.Optional("rowName", "");

        driver.lookAtDetails(listName, rowName);
    }

    public void assertMemberWasSelected(String... args) {
        Params params = new Params(args);
        String memberEmail = params.Optional("memberEmail", "");

        driver.assertMemberWasSelected(memberEmail);
    }

    public void sendEmailToMember(String... args) {
        Params params = new Params(args);
        String memberEmail = params.Optional("memberEmail", "");

        driver.sendEmailToMember(memberEmail);
    }

    public void openQuestionToMember(String... args) {
        Params params = new Params(args);
        String memberEmail = params.Optional("memberEmail", "");

        driver.openQuestionToMember(memberEmail);
    }

    public void selectMemberAnswer(String... args) {
        Params params = new Params(args);
        String answerId = params.Optional("answerId", "");

        driver.selectMemberAnswer(answerId);
    }

    public void assertIsEmailSent(String... args) {
        Params params = new Params(args);
        String memberEmail = params.Optional("memberEmail", "");
        driver.assertIsEmailSent(memberEmail);
    }

    public void assertMemberAnswered(String... args) {
        Params params = new Params(args);
        String memberEmail = params.Optional("memberEmail", "");
        driver.assertMemberAnswered(memberEmail);
    }

    public void assertStats(String... args) {
        Params params = new Params(args);

        String selectedMembersCount = params.Optional("Wybraneosoby", "");
        String sentEmailsCount = params.Optional("Wysłane", "");
        String answersCount = params.Optional("Odpowiedzi", "");
        String notAnsweredCount = params.Optional("Bezodpowiedzi", "");
        String yesCount = params.Optional("Tak", "");
        String noCount = params.Optional("Nie", "");
        String laterCount = params.Optional("Odpowiempóźniej", "");

        driver.assertStats(selectedMembersCount, sentEmailsCount, answersCount, notAnsweredCount, yesCount, noCount, laterCount);
    }

    public void assertPersonHasFields(String... args) {
        Params params = new Params(args);
        String firstName = params.Optional("firstName", "Default");
        String lastName = params.Optional("lastName", "Default");
        String email = params.Optional("email", "d@d.pl");

        String defaultTag = params.Optional("defaultTag", null);

        driver.assertPersonHasFields(firstName, lastName, email, defaultTag);
    }

    public void assertPersonNotExists(String... args) {
        Params params = new Params(args);
        String firstName = params.Optional("firstName", null);

        driver.assertPersonNotExists(firstName);
    }
}
