package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pl.bzowski.drivers.RegisteredUser;

@QuarkusTest
public class PersonsTest extends MyTestsBase {


    @Test
    public void shouldAddAPersonWithAConductorTag() {
        registeredUser.lookAtTagsList();
        registeredUser.askToCreateNewTag();
        registeredUser.fillNewTagDetails("name: Conductor");
        registeredUser.confirmNewTag();

        registeredUser.lookAtPersonsList();
        registeredUser.askToCreateNewPerson();
        registeredUser.fillNewPersonDetails("firstName: Kontrybutor", "lastName: Kowalski", "email: kontrybutor.kowalski@gmail.com");
        registeredUser.confirmNewPerson();

        registeredUser.assertNewPersonCreated("firstName: Kontrybutor");
    }

}
