package dev.karina;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;

import Entities.Credencial;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.specification.RequestSpecification;
public class BookingTests {
    

    public static RequestSpecification request;
    private static Credencial credencial;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        
    }

    @BeforeEach
    void setRequest() {
        Properties prop = Manipular.getProp();
        String userName = prop.getProperty("username");
        String password = prop.getProperty("password");
        request = RestAssured.given()
                .config(RestAssuredConfig.config()
                        .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic(userName, password);
    }

    @Test
    public void Ping_HealthCheck_Return201() {
        request
                .when()
                .get("/ping")
                .then()
                .assertThat().statusCode(201);
    }

    @Test
    public void CreateToken_WithValidData_ReturnOk() {
        Properties prop = Manipular.getProp();
        String userName = prop.getProperty("username");
        String password = prop.getProperty("password");
        credencial = new Credencial(userName, password);
        request
                .body(credencial)
                .when()
                .post("/auth")
                .then()
                .assertThat().statusCode(200).and()
                .body("token", Matchers.isA(String.class));
    }

   
}
