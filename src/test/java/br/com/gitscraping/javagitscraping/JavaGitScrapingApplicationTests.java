package br.com.gitscraping.javagitscraping;


import org.springframework.boot.test.context.SpringBootTest;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JavaGitScrapingApplicationTests {

	@Test
    public void ApiTest() {
        // Set up the base URI for API calls
        RestAssured.baseURI = determineBaseURI();

        // Do the HTTP GET request to the needed route
        Response response = given()
                .param("rep", "gabrielgcosta/botcopa")
                .when()
                .get("/git-info");

        // Check if the result is correct
        assertEquals(200, response.getStatusCode());
		assertTrue(response.getBody().asString().contains("extension"));
		assertTrue(response.getBody().asString().contains("count"));
		assertTrue(response.getBody().asString().contains("lines"));
		assertTrue(response.getBody().asString().contains("bytes"));


    }

    private String determineBaseURI() {
        return "https://java-scraping-no-api-2464b5478565.herokuapp.com";
    }

}
