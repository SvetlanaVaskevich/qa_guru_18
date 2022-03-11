package bk.vaskevich;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class DemoWebShopTest {

    @BeforeAll
    static void configureBaseUrl() {
        RestAssured.baseURI = "http://demowebshop.tricentis.com/";
        Configuration.baseUrl = "http://demowebshop.tricentis.com/";
    }

    @Test
    public void addProductToCardTest(){

        Map<String, String> autorizationCookie =
                given()
                        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                        .formParam("Email", "qaguru@qa.guru")
                        .formParam("Password", "qaguru@qa.guru1")
                        .when()
                        .post("/login")
                        .then()
                        .statusCode(302)
                        .extract()
                        .cookies();

        //Nop.customer=68e7d841-ee2a-44fd-973e-18721229228e, NOPCOMMERCE.AUTH=65BCCFE0AD0B3DB2FA7558098CD55345FA2B6EF65D9406D67C5D601353506068306D8FAF006D5EFB6A97313AA6B55BDA0FD88B1E46AC1B23D5CD7EB54B9314B7665E342B206D2095129C68DA2FC14BB7FA10DFF1E12AF6AC097C80E93648C2A1681B4EBBA18ED40E32128A53BA2CBB6727696BDC6E2555E3CC1CA2D509F57E50A1059C32E24F00647049F5CB08152A0A, ARRAffinity=a1e87db3fa424e3b31370c78def315779c40ca259e77568bef2bb9544f63134e

        open("/Themes/DefaultClean/Content/images/logo.png");

        getWebDriver().manage().addCookie(new Cookie("NOPCOMMERCE.AUTH",
                autorizationCookie.get("NOPCOMMERCE.AUTH")));

        open("/cart");
        String cartSize = $("#topcartlink .cart-qty").getText();

        Integer cartSizeBeforeAdded = parseToIntegerCart(cartSize);
        System.out.println("Cart size before add " + cartSizeBeforeAdded);

             Response response =
                     given()
                             .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                             .cookie(autorizationCookie.get("Nop.customer")+";")
                             .body("addtocart_31.EnteredQuantity=1")
                             .when()
                             .post("/addproducttocart/details/31/1")
                             .then()
                             .log().all()
                             .statusCode(200)
                             .body("success", is(true))
                             .extract().response();

        String actualCartString = response.path("updatetopcartsectionhtml").toString();

        Integer actualCartSize = parseToIntegerCart(actualCartString);
        System.out.println("Cart size after add " + actualCartSize);

        assertThat(actualCartSize.compareTo(cartSizeBeforeAdded));
    }

    @AfterEach
    void close(){
        WebDriverRunner.closeWebDriver();
    }

    public static Integer parseToIntegerCart(String cartSizeString) {
        Integer cartSize;
        String[] numbers = cartSizeString.split("\\D+");
        cartSize = Integer.parseInt(String.join("", numbers));
        return cartSize;
    }
}

