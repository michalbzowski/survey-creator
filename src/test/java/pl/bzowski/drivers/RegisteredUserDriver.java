package pl.bzowski.drivers;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.utils.Driver;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@Singleton
public class RegisteredUserDriver implements Driver {

    private static final Logger log = LoggerFactory.getLogger(RegisteredUserDriver.class);
    private final WebDriver driver;

    @ConfigProperty(name = "app.host")
    String appHost;

    public RegisteredUserDriver() {
        WebDriverManager.chromedriver().setup();  // tutaj WebDriverManager pobiera i ustawia chromedriver
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @Override
    public void openSystem() {
        try {
            driver.get(appHost + "/web/tags");

            while (!driver.getCurrentUrl().contains("0.0.0.0:9999/realms/master")) {
                Thread.sleep(500);
            }

            // Wypełnij login i hasło (selktory dla keycloak - mogą się różnić, sprawdź w inspektorze)
            WebElement usernameInput = driver.findElement(By.id("username"));
            WebElement passwordInput = driver.findElement(By.id("password"));

            usernameInput.sendKeys("user");
            passwordInput.sendKeys("!User123#");

            // Kliknij przycisk zaloguj
            WebElement loginButton = driver.findElement(By.id("kc-login"));
            loginButton.click();
            // Teraz po zalogowaniu powinieneś wrócić na właściwą stronę
            while (!driver.getCurrentUrl().contains(appHost + "")) {
                Thread.sleep(500);
            }
            // Tutaj możesz wykonać dalsze akcje np. sprawdzenie, czy tag się pojawił
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void fillNewTagDetails(String name) {
        clickAndSendKeys("name", name);
    }

    @Override
    public void submitNewTag() {
        log.info("confirmNewTag - start");
        WebElement element = driver.findElement(By.id("submit"));
        element.click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> {
                    log.info("confirmNewTag - wait");
                    return !webDriver.getCurrentUrl().contains("/web/tags/new");
                });
        log.info("confirmNewTag - stop");
    }

    @Override
    public void assertNewTagCreated(String name) {
        WebElement element = driver.findElement(By.id(name));
        String text = element.getText();
        assertThat(text).isEqualTo(name);
    }

    public void askToCreateNewTag() {
        WebElement element = driver.findElement(By.cssSelector("a[href='/web/tags/new']"));
        element.click();
    }

    @Override
    public void deleteTag(String name) {
        WebElement link = driver.findElement(By.xpath("//tr[td[@id='" + name + "' and text()='" + name + "']]//a[@class='delete-tag']"));
        link.click();
        try {
            // Przełącz się do alertu
            Alert alert = driver.switchTo().alert();

            // Możesz odczytać tekst alertu (opcjonalne)
            String alertText = alert.getText();
            System.out.println("Alert text: " + alertText);

            // Zaakceptuj alert (kliknij "OK")
            alert.accept();
            System.out.println("accepted");

            // Lub jeśli chcesz anulować (kliknąć "Cancel"), to daj alert.dismiss();
        } catch (NoAlertPresentException e) {
            // Alert nie pojawił się - możesz obsłużyć ten przypadek
        }
    }

    @Override
    public void assertTagNotExists(String name) {
        List<WebElement> elements = driver.findElements(By.id(name));
        assertThat(elements).isEmpty();
    }

    @Override
    public void lookAtPersonsList() {
        log.info("lookAtPersonsList - start go to /web/persons. Current: " + driver.getCurrentUrl());
        driver.get(appHost + "/web/persons");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Current URL after get(): " + driver.getCurrentUrl());
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/web/persons"));
        log.info("lookAtPersonsList - stop go to /web/persons. Current: " + driver.getCurrentUrl());
    }


    @Override
    public void askToCreateNewPerson() {
        By by = By.cssSelector("a[href='/web/persons/new']");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
        element.click();
    }

    @Override
    public void fillNewPersonDetails(String firstName, String lastName, String email, String defaultTag, String groups) {
        clickAndSendKeys("firstName", firstName);
        clickAndSendKeys("lastName", lastName);
        clickAndSendKeys("email", email);
        if (defaultTag != null) {
            //TODO
        }
        if (groups != null) {
            //TODO
        }
    }

    protected void clickAndSendKeys(String id, String valie) {
        WebElement element = driver.findElement(By.id(id));
        element.click();
        element.sendKeys(valie);
    }

    @Override
    public void confirmNewPerson() {
        WebElement element = driver.findElement(By.id("submit"));
        element.click();
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))       // maksymalny czas czekania
                .pollingEvery(Duration.ofMillis(500))      // co 500ms sprawdzanie warunku
                .ignoring(Exception.class);                 // ignorowanie wyjątków podczas oczekiwania

        wait.until(driver -> Objects.requireNonNull(driver.getCurrentUrl()).contains(appHost + "/web/persons"));
    }

    @Override
    public void assertNewPersonCreated(String firstName) {
        WebElement element = driver.findElement(By.cssSelector("td[data-fist-name=\"" + firstName + "\"]"));
        String text = element.getText();
        assertThat(text).isEqualTo(firstName);
    }

    @Override
    public void lookAtTagsList() {
        driver.get(appHost + "/web/tags");
    }

    @Override
    public void exit() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            log.warn("Problem closing WebDriver", e);
        } finally {
            if (driver != null) {
                log.info("quit");
                driver.quit();
            }
        }
    }


}
