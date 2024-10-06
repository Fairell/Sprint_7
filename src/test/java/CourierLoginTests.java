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
import static org.hamcrest.Matchers.notNullValue;

public class CourierLoginTests {
    private final String BASE_URI = "http://qa-scooter.praktikum-services.ru";
    private final String CREATE_COURIER_ENDPOINT = "/api/v1/courier";
    private final String LOGIN_COURIER_ENDPOINT = "/api/v1/courier/login";
    private final String DELETE_COURIER_ENDPOINT = "/api/v1/courier/";

    private String courierId;
    private String login;
    private String password;

    @Before
    public void setup() {
        RestAssured.baseURI = BASE_URI;

        // Создаем курьера перед тестом для проверки успешной авторизации
        login = generateRandomLogin();
        password = generateRandomPassword();
        String firstName = generateRandomFirstName();

        Response createResponse = createCourier(login, password, firstName);
        createResponse.then().statusCode(201);
        courierId = createResponse.jsonPath().getString("id");
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

    @Step("Авторизация курьера с логином: {login} и паролем: {password}")
    private Response loginCourier(String login, String password) {
        String requestBody = String.format("{ \"login\": \"%s\", \"password\": \"%s\" }", login, password);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(LOGIN_COURIER_ENDPOINT);
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
        String[] names = {"John", "Jane", "Saske", "Naruto", "Sakura", "Kakashi", "Hinata"};
        Random random = new Random();
        return names[random.nextInt(names.length)];
    }

    @Step("Проверка успешной авторизации курьера")
    @Test
    public void courierCanLoginSuccessfully() {
        Response response = loginCourier(login, password);
        response.then().statusCode(200)
                .body("id", notNullValue());  // Проверка, что в ответе есть ID
    }

    @Step("Проверка авторизации курьера без логина")
    @Test
    public void courierCannotLoginWithoutLogin() {
        Response response = loginCourier("", password);
        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Step("Проверка авторизации курьера без пароля")
    @Test
    public void courierCannotLoginWithoutPassword() {
        Response response = loginCourier(login, "");
        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Step("Проверка авторизации курьера с несуществующими данными")
    @Test
    public void courierCannotLoginWithInvalidCredentials() {
        Response response = loginCourier(generateRandomLogin(), generateRandomPassword());
        response.then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}
