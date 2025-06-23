import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:4004"; // Set the base URI for the API
        // This method is called before any tests are run.
        // You can initialize resources or set up the environment here.
        System.out.println("Setting up the integration test environment...");
    }

    @Test
    public void shouldReturnPatientsWithValidToken() {
        // Arrange
        String loginPayload = """
                {
                     "email": "testuser@test.com",
                     "password": "password123"                       
                }
                 """;

        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        System.out.println("Generated Token: " + token);

        // Act
        // Assert
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue()) // Check that the response contains a 'patients' field
                .log().all(); // Log the response for debugging
    }
}
