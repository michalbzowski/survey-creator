package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
@QuarkusTest
public class GroupsTest  extends MyTestsBase {

    @Test
    public void shouldAddConductorTag() {
        registeredUser.lookAtList("groups");
        registeredUser.askToCreateNew();
        registeredUser.fillFormFields("groupName: Conductor");
        registeredUser.confirm("groups");
        registeredUser.assertNewGroupCreated("groupName: Conductor");
    }
}
