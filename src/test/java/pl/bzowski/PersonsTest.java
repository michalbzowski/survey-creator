package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PersonsTest extends MyTestsBase {

    @Test
    public void shouldAddAPersonWithAConductorTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Conductor");
        registeredUser.confirmNewTag();

        registeredUser.lookAtList("persons");
        registeredUser.askToCreateNew();
        registeredUser.fillNewPersonDetails("firstName: Kontrybutor", "lastName: Kowalski", "email: kontrybutor.kowalski@gmail.com");
        registeredUser.confirmNewPerson();

        registeredUser.assertNewPersonCreated("firstName: Kontrybutor");
    }

}
