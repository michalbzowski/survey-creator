package pl.bzowski;

import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import pl.bzowski.drivers.RegisteredUser;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MyTestsBase {

    @Inject
    protected RegisteredUser registeredUser;

    @BeforeAll
    public void beforeAll() {
        registeredUser.openSystem();
    }

    @AfterAll
    public void afterAll() {
        registeredUser.exit();
    }
}
