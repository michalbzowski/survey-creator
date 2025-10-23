package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PersonsTest extends MyTestsBase {

    @Test
    public void shouldNotAddAPersonWithoutATag() {
        registeredUser.lookAtList("persons");
        registeredUser.askToCreateNew();
        registeredUser.fillNewPersonDetails("firstName: KontrybutorXXX", "lastName: KowalskiXXX", "email: kontrybutorXXX.kowalskiXXX@gmail.com");
        registeredUser.confirmNewPerson();
        registeredUser.lookAtList("persons");
        registeredUser.assertPersonNotExists("firstName: KontrybutorXXX");
    }

    @Test
    public void shouldAddAPerson() {
        registeredUser.lookAtList("persons");
        registeredUser.askToCreateNew();
        registeredUser.fillNewPersonDetails("firstName: Kontrybutor", "lastName: Kowalski", "email: kontrybutor.kowalski@gmail.com", "defaultTag: Puzon");
        registeredUser.confirmNewPerson();

        registeredUser.assertNewPersonCreated("firstName: Kontrybutor");
    }

    @Test
    public void shouldAddAPersonWithAConductorTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Conductor");
        registeredUser.confirmNewTag();

        registeredUser.lookAtList("persons");
        registeredUser.askToCreateNew();
        registeredUser.fillNewPersonDetails("firstName: Kontrybutor2", "lastName: Kowalski2", "email: kontrybutor2.kowalski2@gmail.com", "defaultTag: Conductor");
        registeredUser.confirmNewPerson();

        registeredUser.assertNewPersonCreated("firstName: Kontrybutor2");
        registeredUser.assertPersonHasFields("firstName: Kontrybutor2", "lastName: Kowalski2", "email: kontrybutor2.kowalski2@gmail.com", "defaultTag: Conductor");
    }

    @Test
    @Disabled
    public void shouldEditFirstName() {

    }


    @Test
    @Disabled
    public void shouldEditFirstLastName() {

    }

    @Test
    @Disabled
    public void shouldEditEmailAndPersonShouldConfirmOnceAgainEmail() {

    }


    @Test
    @Disabled
    public void shouldEditDefaultTag() {

    }

    @Test
    @Disabled
    public void shouldEditGroups() {

    }

    @Test
    @Disabled
    public void shouldAddNewPersonAndSendEmailWithoutFromEmailConfiguration() {

    }

    @Test
    @Disabled
    public void shouldAddNewPersonAndSendEmailWithFromEmailConfiguration() {

    }

    @Test
    @Disabled
    public void shouldPersonConfirmAddingToUserPersonsList() {

    }

}
