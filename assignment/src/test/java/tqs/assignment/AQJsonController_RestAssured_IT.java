package tqs.assignment;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tqs.assignment.component.Cache;
import tqs.assignment.entity.*;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author wy
 * @date 2021/5/8 17:52
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AssignmentApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AQJsonController_RestAssured_IT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Cache cache;

    @BeforeEach
    void port(){
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    @Order(1)
    public void givenPorto_returnPorto_CacheSize1(){
        String lisbon = "porto";
        RestAssuredMockMvc.given()
                .get("/air_quality/json/location/{location}/", lisbon)
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("success"))
                .assertThat().body("data.city.name", is("Sobreiras-Lordelo do Ouro, Porto, Portugal"))
                .assertThat().body("data.city.geo.latitude", is("41.15"))
                .assertThat().body("data.city.geo.longitude", is("-8.66"));

        assertEquals(1, cache.size());
    }

    @Test
    @Order(2)
    public void givenPortugalGeo_returnPortugal_thenSize2() {
        RestAssuredMockMvc.given()
                .get("/air_quality/json/geo/{lat}:{lgn}", "32.65", "-16.92")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("success"))
                .assertThat().body("data.city.name" , is("São Gonçalo, Funchal, Portugal"))
                .assertThat().body("data.city.geo.latitude", is("32.65"))
                .assertThat().body("data.city.geo.longitude", is("-16.92"));

        assertEquals(2, cache.size());
    }


    @Test
    @Order(3)
    public void givenInvalidCity_thenNoDataForThisCity(){
        String invalid = "InvalidCity";
        RestAssuredMockMvc.given()
                .get("/air_quality/json/location/{location}/", invalid)
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("error, no data found"));
    }

    @Test
    @Order(4)
    public void givenInvalidGeo_thenStatus4xx(){
        RestAssuredMockMvc.given()
                .get("/air_quality/json/geo/{lat}:{lgn}", "aaa", "qq")
                .then().statusCode(400);
    }

    @Test
    @Order(5)
    public void getCacheStatistics() {
        Statistics statistics = cache.getStatistics();
        RestAssuredMockMvc.given()
                .get("/air_quality/json/cache/statistics")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("success"))
                .assertThat().body("data.size", is(statistics.getSize()))
                .assertThat().body("data.hits", is(statistics.getHits()))
                .assertThat().body("data.misses", is(statistics.getMisses()))
                .assertThat().body("data.collected", is(statistics.getCollected()));
    }
}
