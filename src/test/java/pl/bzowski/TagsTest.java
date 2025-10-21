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
        registeredUser.assertTagExists("name: Conductor");
    }

    @Test
    public void shouldAddWitcherTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Witcher");
        registeredUser.confirmNewTag();
        registeredUser.assertTagExists("name: Witcher");
    }

    @Test
    public void shouldAddCheckRemoveCheckWizardTag() {
        registeredUser.lookAtList("tags");
        registeredUser.askToCreateNew();
        registeredUser.fillNewTagDetails("name: Pierdzisław");
        registeredUser.confirmNewTag();
        registeredUser.assertTagExists("name: Pierdzisław");
        registeredUser.deleteTag("name: Pierdzisław");
        registeredUser.assertTagNotExists("name: Pierdzisław");
    }

    @Test
    public void shouldNotRemoveUsedTag() {
        registeredUser.lookAtList("tags");
        registeredUser.assertTagExists("name: Dyrygent");
        registeredUser.deleteTag("name: Dyrygent");
        registeredUser.assertTagExists("name: Dyrygent");
    }
}
