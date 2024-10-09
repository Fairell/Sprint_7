import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrdersTest {

    private static final String BASE_URL = "http://qa-scooter.praktikum-services.ru/";
    private static final String GET_ORDERS_ENDPOINT = BASE_URL + "api/v1/orders";

    @Test
    @Step("Проверка получения списка заказов без параметров")
    public void testGetOrdersWithoutParameters() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(GET_ORDERS_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200)  // Проверка, что статус ответа - 200 OK
                .body("orders", notNullValue())  // Проверка, что поле "orders" присутствует и не пустое
                .body("orders.size()", greaterThan(0))  // Проверка, что заказы присутствуют в списке
                .body("pageInfo.page", equalTo(0))  // Проверка, что страница по умолчанию равна 0
                .body("pageInfo.limit", equalTo(30));  // Проверка, что лимит по умолчанию равен 30
    }

    @Test
    @Step("Проверка получения списка заказов с параметром courierId = 387417")
    public void testGetOrdersWithCourierId() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", 387417)  // Указание параметра courierId
                .when()
                .get(GET_ORDERS_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200)  // Проверка, что статус ответа - 200 OK
                .body("orders", notNullValue())  // Проверка, что поле "orders" присутствует и не пустое
                .body("orders.size()", greaterThan(0))  // Проверка, что заказы присутствуют в списке
                .body("orders[0].courierId", equalTo(387417));  // Проверка, что courierId соответствует указанному значению
    }
    @Test
    @Step("Проверка получения списка заказов с несуществующим courierId")
    public void testGetOrdersWithNonExistentCourierId() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", 999999)  // Использование несуществующего значения courierId
                .when()
                .get(GET_ORDERS_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(404)  // Проверка, что статус ответа - 404 Not Found
                .body("message", equalTo("Курьер с идентификатором 999999 не найден"));  // Проверка сообщения об ошибке
    }

}
