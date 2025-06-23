import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:4004"; // Set the base URI for the API
        // This method is called before any tests are run.
        // You can initialize resources or set up the environment here.
        System.out.println("Setting up the integration test environment...");
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        //Arrange
        // Act
        // Assert

        String loginPayload = """
                {
                     "email": "testuser@test.com",
                     "password": "password123"                       
                }
                 """;

        Response response = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        //Arrange
        // Act
        // Assert

        String loginPayload = """
                {
                     "email": "Invalid_user@test.com",
                     "password": "wrongpassword"                       
                }
                 """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);

        System.out.println("Test for invalid login completed, expected 401 Unauthorized response.");
    }
}
