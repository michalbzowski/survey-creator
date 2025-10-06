package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TagsTest extends MyTestsBase {

    @Test
    public void shouldAddConductorTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Conductor");
        registeredUser.confirmNewTag();
        registeredUser.assertNewTagCreated("name: Conductor");
    }

    @Test
    public void shouldAddWitcherTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Witcher");
        registeredUser.confirmNewTag();
        registeredUser.assertNewTagCreated("name: Witcher");
    }

    @Test
    public void shouldAddCheckRemoveCheckWizardTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Wizard");
        registeredUser.confirmNewTag();
        registeredUser.assertNewTagCreated("name: Wizard");
        registeredUser.deleteTag("name: Wizard");
        registeredUser.assertTagNotExists("name: Wizard");
    }
}
