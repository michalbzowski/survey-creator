package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pl.bzowski.drivers.RegisteredUser;

@QuarkusTest
public class GroupsTest extends MyTestsBase {

    @Test
    public void shouldAddEmptyGroup() {
        createGroup(registeredUser, "groupName: EmptyGroup");

        registeredUser.assertNewGroupCreated("groupName: EmptyGroup");
    }

    @Test
    public void shouldAddConductorTag() {
        createGroup(registeredUser, "groupName: Conductor",  "checkboxCount: 1", "checkboxId1: michal.bzowski@gmail.com", "checkboxValue1: True");

        registeredUser.edit("Conductor");
        registeredUser.assertIsChecked("checkboxes: [michal.bzowski@gmail.com]");

    }

    public static void createGroup(RegisteredUser registeredUser, String... args) {
        registeredUser.lookAtList("groups");
        registeredUser.askToCreateNew();
        registeredUser.fillGroupFormFields(args);
        registeredUser.confirm("groups");
    }
}
