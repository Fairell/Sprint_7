import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class CourierCreationTests {
    private final String BASE_URI = "http://qa-scooter.praktikum-services.ru";
    private final String CREATE_COURIER_ENDPOINT = "/api/v1/courier";
    private final String DELETE_COURIER_ENDPOINT = "/api/v1/courier/";

    private String courierId;

    @Before
    public void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    @After
    public void tearDown() {
        if (courierId != null) {
            deleteCourier(courierId);
        }
    }

    @Step("Создание курьера с логином: {login}, паролем: {password} и именем: {firstName}")
    private Response createCourier(String login, String password, String firstName) {
        String requestBody = String.format("{ \"login\": \"%s\", \"password\": \"%s\", \"firstName\": \"%s\" }", login, password, firstName);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(CREATE_COURIER_ENDPOINT);
    }

    @Step("Удаление курьера с ID: {courierId}")
    private void deleteCourier(String courierId) {
        RestAssured.given()
                .when()
                .delete(DELETE_COURIER_ENDPOINT + courierId)
                .then().statusCode(200);
    }

    @Step("Генерация случайного логина")
    private String generateRandomLogin() {
        return "login_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Step("Генерация случайного пароля")
    private String generateRandomPassword() {
        return "pass_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Step("Генерация случайного имени")
    private String generateRandomFirstName() {
        String[] names = {"John", "Jane", "Saske", "Naruto", "Sakura", "Takashi", "Hinata"};
        Random random = new Random();
        return names[random.nextInt(names.length)];
    }

    @Step("Проверка успешного создания курьера")
    @Test
    public void createCourierSuccessfully() {
        String login = generateRandomLogin();
        String password = generateRandomPassword();
        String firstName = generateRandomFirstName();

        Response response = createCourier(login, password, firstName);
        response.then().statusCode(201)
                .body("ok", equalTo(true));

        courierId = response.jsonPath().getString("id");
    }

    @Step("Проверка создания курьера с повторяющимся логином")
    @Test
    public void createDuplicateCourier() {
        String login = generateRandomLogin();
        String password = generateRandomPassword();
        String firstName = generateRandomFirstName();

        createCourier(login, password, firstName).then().statusCode(201);

        Response response = createCourier(login, password, firstName);
        response.then().statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Step("Проверка создания курьера без логина")
    @Test
    public void createCourierWithoutLogin() {
        String password = generateRandomPassword();
        String firstName = generateRandomFirstName();

        Response response = createCourier("", password, firstName);
        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Проверка создания курьера без пароля")
    @Test
    public void createCourierWithoutPassword() {
        String login = generateRandomLogin();
        String firstName = generateRandomFirstName();

        Response response = createCourier(login, "", firstName);
        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }
}
