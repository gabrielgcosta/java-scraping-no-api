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
        // Configure a base URI para suas chamadas de API
        RestAssured.baseURI = determineBaseURI();

        // Faça a solicitação HTTP GET para a rota desejada
        Response response = given()
                .param("rep", "gabrielgcosta/botcopa")
                .when()
                .get("/git-info");

        // Verifique se o status da resposta é 200
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
