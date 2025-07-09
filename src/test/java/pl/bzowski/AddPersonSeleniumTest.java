package pl.bzowski;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.*;

public class AddPersonSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // Set path to chromedriver if needed
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldAddNewPerson() {
        driver.get("http://localhost:8080/persons/add");

        driver.findElement(By.name("firstName")).sendKeys("John");
        driver.findElement(By.name("lastName")).sendKeys("Doe");
        driver.findElement(By.name("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.name("defaultTag")).sendKeys("VIP");

        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Wait for redirect and check if the person appears in the list
        WebElement personRow = driver.findElement(By.xpath("//td[contains(text(),'john.doe@example.com')]"));
        assertNotNull(personRow);
    }
}
