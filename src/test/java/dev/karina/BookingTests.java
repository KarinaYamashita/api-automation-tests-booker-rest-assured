package dev.karina;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.javafaker.Faker;

import dev.karina.Entities.Booking;
import dev.karina.Entities.BookingDates;
import dev.karina.Entities.Credencial;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingTests {

    public static RequestSpecification request;
    private static Credencial credencial;
    private static Booking booking;
    private static Faker faker;
    private static String bookingId;
    private static Response response;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static String token;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        Date checkin = faker.date().future(1, TimeUnit.DAYS);
        Date checkout = faker.date().future(10, TimeUnit.DAYS, checkin);
        BookingDates bookingdates = new BookingDates(dateFormat.format(checkin), dateFormat.format(checkout));
        booking = new Booking(faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(0, 1000),
                faker.bool().bool(),
                bookingdates,
                faker.lorem().sentence());
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
    @Order(1)
    public void Ping_HealthCheck_Return201() {
        request
                .when()
                .get("/ping")
                .then()
                .assertThat().statusCode(201);
    }

    @Test
    @Order(2)
    public void CreateToken_WithValidData_ReturnOk() {
        Properties prop = Manipular.getProp();
        String userName = prop.getProperty("username");
        String password = prop.getProperty("password");
        credencial = new Credencial(userName, password);
        response = request
                .body(credencial)
                .when()
                .post("/auth")
                .then()
                .assertThat().statusCode(200).and()
                .body("token", Matchers.isA(String.class))
                .extract().response();
        token = response.path("token");
    }

    @Test
    @Order(3)
    public void CreateBooking_WithValidData_ReturnOk() {
        response = request
                .body(booking)
                .when()
                .post("/booking")
                .then()
                .log().all()
                .assertThat().statusCode(200).and()
                .time(Matchers.lessThan(2000L)).and()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("createBookingResponseSchema.json"))
                .extract().response();
        bookingId = response.path("bookingid").toString();
    }

    @Test
    @Order(4)
    public void GetBookingIds_AllIds_ReturnOk() {
        request
                .when()
                .get("/booking")
                .then()
                .assertThat().statusCode(200)
                .contentType(ContentType.JSON)
                .and().body("results", Matchers.hasSize(Matchers.greaterThan(0)));
    }

    @Test
    @Order(5)
    public void GetBookingIds_FilerByName_ReturnOk() {
        request
                .when()
                .get("/booking?firstname=" + booking.getFirstname() + "&lastname=" + booking.getLastname())
                .then()
                .assertThat().statusCode(200)
                .contentType(ContentType.JSON)
                .and().body("results", Matchers.hasSize(Matchers.greaterThan(0)));
    }

    @Test
    @Order(6)
    public void GetBookingIds_FilerByCkeckinCheckoutDate_ReturnOk() {
        request
                .when()
                .queryParam("checkout", booking.getBookingdates().getCheckout())
                .get("/booking")
                .then()
                .assertThat().statusCode(200)
                .contentType(ContentType.JSON)
                .and().body("results", Matchers.hasSize(Matchers.greaterThan(0)));
    }

    @Test
    @Order(7)
    public void GetBooking_WithValidData_ReturnOk() {
        request
                .when()
                .get("/booking/" + bookingId)
                .then()
                .assertThat().statusCode(200).and()
                .time(Matchers.lessThan(2000L)).and()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("createBookingRequestSchema.json"));
    }

    @Test
    @Order(8)
    public void UpdateBooking_WithValidData_ReturnOk() {
        faker = new Faker();
        Date checkin = faker.date().future(1, TimeUnit.DAYS);
        Date checkout = faker.date().future(10, TimeUnit.DAYS, checkin);
        BookingDates bookingdates = new BookingDates(dateFormat.format(checkin), dateFormat.format(checkout));
        Booking booking2 = new Booking(faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(0, 1000),
                faker.bool().bool(),
                bookingdates,
                faker.lorem().sentence());
        request
                .header("Cookie", "token=" + token)
                .contentType(ContentType.JSON)
                .body(booking2)
                .when()
                .put("/booking/" + bookingId)
                .then()
                .log().all()
                .assertThat().statusCode(200).and()
                .time(Matchers.lessThan(2000L)).and()
                .body("firstname", Matchers.equalTo(booking2.getFirstname()))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("createBookingRequestSchema.json"));
    }

    @Test
    @Order(9)
    public void PartialUpdateBooking_WithValidData_ReturnOk() {
        faker = new Faker();
        String firstNamePartialUpdate = faker.name().firstName();
        String lastNamePartialUpdate = faker.name().lastName();
        String bookingString = "{\n" +
                " \"firstname\": \"" + firstNamePartialUpdate + "\" ,\n" +
                " \"lastname\": \"" + lastNamePartialUpdate + "\"    \n}";
        request
                .header("Cookie", "token=" + token)
                .contentType(ContentType.JSON)
                .body(bookingString)
                .when()
                .patch("/booking/" + bookingId)
                .then()
                .log().all()
                .assertThat().statusCode(200).and()
                .time(Matchers.lessThan(2000L)).and()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("createBookingRequestSchema.json"));
    }

    @Test
    @Order(10)
    public void DeleteBooking_UserExists_ReturneOk() {
        request
                .header("Cookie", "token=" + token)
                .when()
                .delete("/booking/" + bookingId)
                .then()
                .assertThat().statusCode(201)
                .and().time(Matchers.lessThan(2000L))
                .log();
    }

}
