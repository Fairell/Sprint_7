import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    private static final String BASE_URL = "http://qa-scooter.praktikum-services.ru/";
    private static final String CREATE_ORDER_ENDPOINT = BASE_URL + "api/v1/orders";

    private final String[] colors;  // Параметр для цвета заказа
    private int trackNumber;  // Номер трека заказа, который будет использоваться для удаления заказа

    public CreateOrderTest(String[] colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters(name = "Цвета заказа: {0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}},
                {new String[]{"GREY"}},
                {new String[]{"BLACK", "GREY"}},
                {new String[]{}}
        });
    }

    @Test
    @Step("Проверка создания заказа с цветами {0}")
    public void testCreateOrderWithDifferentColors() {
        // Создаём данные заказа и сразу передаём их в запрос.
        Order order = new Order(
                "Naruto",         // Имя заказчика
                "Uchiha",         // Фамилия заказчика
                "Konoha, 142 apt.",  // Адрес заказчика
                4,                // Ближайшая станция метро
                "+7 800 355 35 35", // Телефон
                5,                // Время аренды в днях
                "2020-06-06",     // Дата доставки
                "Saske, come back to Konoha", // Комментарий
                colors            // Массив цветов
        );

        trackNumber = given()
                .contentType(ContentType.JSON)
                .body(order)  // Передача объекта order в теле запроса
                .when()
                .post(CREATE_ORDER_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(201)
                .and()
                .body("track", notNullValue())
                .extract()
                .path("track");
    }

    @Step("Отмена заказа с трек-номером {0}")
    public void cancelOrder(int trackNumber) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .put(BASE_URL + "api/v1/orders/cancel/" + trackNumber)
                .then()
                .statusCode(200);
    }

    // Вложенный класс Order должен быть статическим.
    @JsonInclude(JsonInclude.Include.NON_NULL)  // Не включать поля со значением null в JSON
    private static class Order {
        @JsonProperty("firstName")
        private final String firstName;

        @JsonProperty("lastName")
        private final String lastName;

        @JsonProperty("address")
        private final String address;

        @JsonProperty("metroStation")
        private final int metroStation;

        @JsonProperty("phone")
        private final String phone;

        @JsonProperty("rentTime")
        private final int rentTime;

        @JsonProperty("deliveryDate")
        private final String deliveryDate;

        @JsonProperty("comment")
        private final String comment;

        @JsonProperty("color")
        private final String[] color;

        public Order(String firstName, String lastName, String address, int metroStation, String phone, int rentTime, String deliveryDate, String comment, String[] color) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.metroStation = metroStation;
            this.phone = phone;
            this.rentTime = rentTime;
            this.deliveryDate = deliveryDate;
            this.comment = comment;
            this.color = color;
        }
    }
}
