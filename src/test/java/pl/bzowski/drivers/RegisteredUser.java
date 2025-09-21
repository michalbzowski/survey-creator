package pl.bzowski.drivers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pl.bzowski.utils.Driver;
import pl.bzowski.utils.Params;

@Singleton
public class RegisteredUser {

    @Inject
    private Driver driver;

    public void openSystem() {
        driver.openSystem();
    }

    public void lookAtTagsList() {
        driver.lookAtTagsList();
    }

    public void askToCreateNewTag() {
        driver.askToCreateNewTag();
    }

    public void fillNewTagDetails(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "Dyrygent");

        driver.fillNewTagDetails(name);
    }

    public void confirmNewTag() {
        driver.submitNewTag();
    }

    public void assertNewTagCreated(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "Dyrygent");

        driver.assertNewTagCreated(name);
    }

    public void exit() {
        driver.exit();
    }


    public void deleteTag(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "");

        driver.deleteTag(name);
    }

    public void assertTagNotExists(String... args) {
        Params params = new Params(args);
        String name = params.Optional("name", "");

        driver.assertTagNotExists(name);
    }

    public void lookAtPersonsList() {
        driver.lookAtPersonsList();
    }

    public void askToCreateNewPerson() {
        driver.askToCreateNewPerson();
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

    public void assertNewPersonCreated(String... args) {
        Params params = new Params(args);
        String firstName = params.Optional("firstName", "Default");
        driver.assertNewPersonCreated(firstName);
    }
}
