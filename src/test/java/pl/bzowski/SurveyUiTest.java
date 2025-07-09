package pl.bzowski;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SurveyUiTest {

    static WebDriver driver;
    static UUID queryId;
    static UUID linkToken;

    @BeforeAll
    public static void setup() {
        driver = new ChromeDriver(); // upewnij się, że chromedriver jest na PATH
        RestAssured.baseURI = "http://localhost:8080";
    }

    @AfterAll
    public static void teardown() {
        driver.quit();
    }

    @Test
    public void lol() {
        addPersonsAndQuery();
        generateLinks();
        fillFormAndSubmit();
        verifyAnswerSaved();
    }

    public void addPersonsAndQuery() {
        // Dodaj 2 osoby
        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("firstName", "Anna", "lastName", "Nowak", "email", "anna@example.com"))
                .post("/persons").then().statusCode(201);

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("firstName", "Jan", "lastName", "Kowalski", "email", "jan@example.com"))
                .post("/persons").then().statusCode(201);

        // Dodaj zapytanie
        var response = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("title", "Czy bierzesz udział?", "description", "Prosimy o odpowiedź."))
                .post("/queries").then().statusCode(201).extract();

        queryId = UUID.fromString(response.path("id"));
        assertNotNull(queryId);
    }

    public void generateLinks() {
        RestAssured.given().contentType(ContentType.JSON)
                .body("")
                .post("/links/generate/" + queryId)
                .then()
                .statusCode(200);

        // Pobierz linki
        var links = RestAssured.get("/links").then().statusCode(200).extract().jsonPath().getList("");
        assertEquals(2, links.size());

        // Weź pierwszy link
        linkToken = UUID.fromString(((Map<String, Object>) links.get(0)).get("linkToken").toString());
        assertNotNull(linkToken);
    }

    public void fillFormAndSubmit() {
        driver.get("http://localhost:8080/respond/" + linkToken);

        WebElement select = driver.findElement(By.id("answer"));
        select.click();
        select.findElement(By.cssSelector("option[value='YES']")).click();

        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
        submit.click();

        // Można też sprawdzić tekst potwierdzenia
        assertTrue(driver.getPageSource().contains("Dziękujemy"));
    }

    public void verifyAnswerSaved() {
        var links = RestAssured.get("/links").then().statusCode(200).extract().jsonPath().getList("");

        boolean found = links.stream().anyMatch(link -> {
            Map<String, Object> l = (Map<String, Object>) link;
            return l.get("linkToken").toString().equals(linkToken.toString()) &&
                    "YES".equals(l.get("answer"));
        });

        assertTrue(found, "Odpowiedź 'YES' została zapisana");
    }
}
