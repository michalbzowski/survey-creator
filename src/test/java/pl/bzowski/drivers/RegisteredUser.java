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

    public void fillFormFields(String... args) {
        Params params = new Params(args);
        String groupName = params.Optional("groupName", "Default");
        String checkboxId = params.Optional("persons#checkbox", null);

        driver.fillFormFields("groupName", groupName);
        if (checkboxId != null) {
            driver.check(checkboxId);
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

    public void assertNewTagCreated(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "Dyrygent");

        driver.assertNewTagCreated(name);
    }

    public void assertIsChecked(String... checkboxes) {
        Params params = new Params(checkboxes);
        String checboxesIds = params.Optional("checkboxes", "[]");
        Arrays.stream(checboxesIds.substring(1, checboxesIds.length() - 1).split(",")).toList()
                .forEach(s -> {
                    String id = s.trim();
                    driver.isChecked(id);
                });
    }
}
