package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
@QuarkusTest
public class GroupsTest  extends MyTestsBase {

    @Test
    public void shouldAddEmptyGroup() {
        registeredUser.lookAtList("groups");
        registeredUser.askToCreateNew();
        registeredUser.fillFormFields("groupName: EmptyGroup");
        registeredUser.confirm("groups");
        registeredUser.assertNewGroupCreated("groupName: EmptyGroup");
    }

    @Test
    public void shouldAddConductorTag() {
        registeredUser.lookAtList("groups");
        registeredUser.askToCreateNew();
        registeredUser.fillFormFields("groupName: Conductor", "persons#checkbox: michal.bzowski@gmail.com");
        registeredUser.confirm("groups");

        registeredUser.edit("Conductor");
        registeredUser.assertIsChecked("checkboxes: [michal.bzowski@gmail.com]");

    }
}
