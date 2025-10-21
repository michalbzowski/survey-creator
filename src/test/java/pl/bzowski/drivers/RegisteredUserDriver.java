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

import static java.util.concurrent.TimeUnit.SECONDS;
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
            while (!driver.getCurrentUrl().contains(appHost)) {
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
    public void assertTagExists(String name) {
        WebElement element = driver.findElement(By.id(name));
        String text = element.getText();
        assertThat(text).isEqualTo(name);
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
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> {
                    log.info("assertTagNotExists - wait");
                    return driver.findElements(By.id(name)).isEmpty();
                });
        List<WebElement> elements = driver.findElements(By.id(name));
        assertThat(elements).isEmpty();
    }

    @Override
    public void lookAtList(String listName) {
        log.info("lookAtPersonsList - start go to /web/" + listName + ". Current: " + driver.getCurrentUrl());
        driver.get(appHost + "/web/" + listName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Current URL after get(): " + driver.getCurrentUrl());
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/web/" + listName));
        log.info("lookAtPersonsList - stop go to /web/" + listName + ". Current: " + driver.getCurrentUrl());
    }


    @Override
    public void askToCreateNew() {
        By by = By.id("create-new");
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
            driver.findElement(By.id("defaultTag")).click();
            driver.findElement(By.id("tag-option-" + defaultTag)).click();
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
    public void fillFormFields(String fieldName, String groupName) {
        clickAndSendKeys(fieldName, groupName);
    }

    @Override
    public void confirm(String formUrl) {
        log.info("confirm new {} - start", formUrl);
        WebElement element = driver.findElement(By.id("submit"));
        element.click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> {
                    log.info("confirm new {} - wait", formUrl);
                    return !webDriver.getCurrentUrl().contains("/web/" + formUrl + "/new");
                });
        log.info("confirm new {} - finished", formUrl);
    }

    @Override
    public void check(String checkboxId, Boolean checkboxValue) {
        WebElement checkbox = driver.findElement(By.id(checkboxId));
        String checked = checkbox.getAttribute("checked");
        if ("true".equals(checked) && Boolean.FALSE.equals(checkboxValue)) {
            checkbox.click();
        }
        if (checked == null && Boolean.TRUE.equals(checkboxValue)) {
            checkbox.click();
        }
    }

    @Override
    public void isChecked(String value) {
        WebElement checkbox = driver.findElement(By.id(value));
        boolean selected = checkbox.isSelected();
        assertThat(selected).isTrue();
    }

    @Override
    public void edit(String rowData) {
        WebElement element = driver.findElement(By.cssSelector("td[data-group-name=\"" + rowData + "\"]"));
        WebElement parent = element.findElement(By.xpath("./.."));
        WebElement link = parent.findElement(By.cssSelector("a.edit-group"));
        String href = link.getDomProperty("href");
        link.click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> {
                    log.info("confirm href {} - wait", href);
                    return webDriver.getCurrentUrl().contains(href);
                });
        log.info("confirm href {} - finished", href);

    }

    @Override
    public void assertExistsOnList(String groupName) {
        WebElement element = driver.findElement(By.cssSelector("td[data-group-name=\"" + groupName + "\"]"));
        String text = element.getText();
        assertThat(text).isEqualTo(groupName);
    }

    @Override
    public void fillFormValue(String id, String value) {
        if (value.isEmpty()) {
            return;
        }
        WebElement element = driver.findElement(By.id(id));
        JavascriptExecutor j = (JavascriptExecutor) driver;
        j.executeScript("arguments[0].value='" + value + "';", element);
    }

    @Override
    public void assertNewEventCreated(String eventName) {
        WebElement element = driver.findElement(By.cssSelector("td[data-events-name=\"" + eventName + "\"]"));
        String text = element.getText();
        assertThat(text).isEqualTo(eventName);
    }

    @Override
    public void select(String radioId) {
        WebElement button = driver.findElement(By.id(radioId));
        button.click();
    }

    @Override
    public void lookAtDetails(String listName, String rowName) {
        WebElement element = driver.findElement(By.cssSelector("td[data-" + listName + "-name=\"" + rowName + "\"]"));
        WebElement parent = element.findElement(By.xpath("./.."));
        WebElement link = parent.findElement(By.cssSelector("a." + listName + "-details"));
        String href = link.getDomProperty("href");
        link.click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> {
                    log.info("confirm href {} - wait", href);
                    return webDriver.getCurrentUrl().contains(href);
                });
        log.info("confirm href {} - finished", href);
    }

    @Override
    public void assertMemberWasSelected(String memberEmail) {
        WebElement element = driver.findElement(By.cssSelector("td[data-member-email=\"" + memberEmail + "\"]"));
        String text = element.getText();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> {
                    log.info("assertMemberWasSelected: {} - wait", memberEmail);
                    return assertThat(text).isEqualTo(memberEmail);
                });
        log.info("assertMemberWasSelected: {} - finished", memberEmail);
    }

    @Override
    public void sendEmailToMember(String memberEmail) {
        WebElement element = driver.findElement(By.cssSelector("td[data-member-email=\"" + memberEmail + "\"]"));
        WebElement parent = element.findElement(By.xpath("./.."));
        WebElement link = parent.findElement(By.cssSelector("a.send-email-button"));
        String href = link.getDomProperty("href");
        link.click();
        System.out.println(href);
    }

    @Override
    public void openQuestionToMember(String memberEmail) {
        WebElement element = driver.findElement(By.cssSelector("td[data-member-email=\"" + memberEmail + "\"]"));
        WebElement parent = element.findElement(By.xpath("./.."));
        WebElement link = parent.findElement(By.cssSelector("a.open-member-question"));
        String href = link.getDomProperty("href");
        link.click();
        System.out.println(href);
    }

    @Override
    public void selectMemberAnswer(String answerId) {
        Object[] windowHandles = driver.getWindowHandles().toArray();
        driver.switchTo().window((String) windowHandles[1]);

        WebElement element = driver.findElement(By.cssSelector("select.answerValue-entry"));
        element.click();

        WebElement answerValueElement = driver.findElement(By.id(answerId));
        answerValueElement.click();

        WebElement submit = driver.findElement(By.id("submit"));
        submit.click();

        driver.switchTo().window((String) windowHandles[0]);
    }

    @Override
    public void assertIsEmailSent(String memberEmail) {
        WebElement element = driver.findElement(By.cssSelector("td[data-member-email=\"" + memberEmail + "\"]"));
        WebElement parent = element.findElement(By.xpath("./.."));
        WebElement emailSentIcon = parent.findElement(By.id("email-send-icon"));
        assertThat(emailSentIcon).isNotNull();
    }

    @Override
    public void assertMemberAnswered(String memberEmail) {
        WebElement element = driver.findElement(By.cssSelector("td[data-member-email=\"" + memberEmail + "\"]"));
        WebElement parent = element.findElement(By.xpath("./.."));
        WebElement emailSentIcon = parent.findElement(By.id("member-answered-icon"));
        assertThat(emailSentIcon).isNotNull();
    }

    @Override
    public void assertStats(String selectedMembersCount, String sentEmailsCount, String answersCount, String notAnsweredCount, String yesCount, String noCount, String laterCount) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class);

        wait.until(_ -> driver.findElement(By.id("summary-tab"))).click();

        var selectedMembersCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("selectedMembersCount"));
            return webElement.getText();
        });
        assertThat(selectedMembersCountText).isEqualTo("Wybrane osoby: " + selectedMembersCount);

        var sentEmailsCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("sentEmailsCount"));
            return webElement.getText();
        });
        assertThat(sentEmailsCountText).isEqualTo("Wysłane: " + sentEmailsCount);

        var answersCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("answersCount"));
            return webElement.getText();
        });
        assertThat(answersCountText).isEqualTo("Odpowiedzi: " + answersCount);

        var notAnsweredCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("notAnsweredCount"));
            return webElement.getText();
        });
        assertThat(notAnsweredCountText).isEqualTo("Bez odpowiedzi: " + notAnsweredCount);

        var yesCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("yesCount"));
            return webElement.getText();
        });
        assertThat(yesCountText).isEqualTo("Tak: " + yesCount);

        var noCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("noCount"));
            return webElement.getText();
        });
        assertThat(noCountText).isEqualTo("Nie: " + noCount);

        var laterCountText = wait.until(x -> {
            WebElement webElement = driver.findElement(By.id("laterCount"));
            return webElement.getText();
        });
        assertThat(laterCountText).isEqualTo("Odpowiem później: " + laterCount);
    }

    @Override
    public void assertPersonHasFields(String firstName, String lastName, String email, String defaultTag) {
        lookAtList("persons");
        lookAtDetails("persons", lastName);
        assertThat(driver.findElement(By.id("firstName")).getAttribute("value")).isEqualTo(firstName);
        assertThat(driver.findElement(By.id("lastName")).getAttribute("value")).isEqualTo(lastName);
        assertThat(driver.findElement(By.id("email")).getAttribute("value")).isEqualTo(email);
        assertThat(driver.findElement(By.id("defaultTag")).getAttribute("value")).isEqualTo(defaultTag);

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
