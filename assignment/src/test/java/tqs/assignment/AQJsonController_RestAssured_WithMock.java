package tqs.assignment;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.assignment.component.Cache;
import tqs.assignment.controller.AirQualityJsonController;
import tqs.assignment.entity.*;
import tqs.assignment.service.AirQualityService;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
/**
 * @author wy
 * @date 2021/5/8 17:52
 */
@WebMvcTest(AirQualityJsonController.class)
public class AQJsonController_RestAssured_WithMock {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private Cache cache;

    @MockBean
    private AirQualityService service;

    private static AirQResponse airQResponse;

    @BeforeAll
    static void setUp() {
        String location = "Aveiro";
        airQResponse = new AirQResponse();
        City city = new City();
        city.setName(location);
        Geo geo = new Geo();
        geo.setLat(10.0);
        geo.setLng(20.0);
        city.setGeo(geo);
        airQResponse.setCity(city);
        airQResponse.setDominentpol("h");

        LinkedHashMap<String, Number> map = new LinkedHashMap<>();
        map.put("p", 10);
        map.put("pm25", 11);
        map.put("t", 12);
        map.put("w", 13);
        map.put("h", 14);
        airQResponse.setIaqis(map);

        Forecast forecast = new Forecast();
        forecast.setTime("2021-05-10");
        forecast.setAvg(11);
        forecast.setMin(2);
        forecast.setMax(20);
        forecast.setIaqi("pm1");
        airQResponse.setForecasts(Map.of("pm1", Arrays.asList(forecast)));
    }

    @BeforeEach
    void port(){
      // RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    public void givenAveiro_returnAveiro(){
        String aveiro = "Aveiro";
        Mockito.when(service.getAirQualityByLocation(aveiro)).thenReturn(airQResponse);

        RestAssuredMockMvc.given()
                .get("/air_quality/json/location/{location}/", aveiro)
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("success"))
                .assertThat().body("data.city.name", is(aveiro))
                .assertThat().body("data.city.geo.latitude", is("10.00"))
                .assertThat().body("data.city.geo.longitude", is("20.00"))
                .assertThat().body("data.dominentpol", is("humidity"))
                .assertThat().body("data.iaqis.pressure", is(10))
                .assertThat().body("data.iaqis.pm25", is(11))
                .assertThat().body("data.iaqis.temperature", is(12))
                .assertThat().body("data.iaqis.wind", is(13))
                .assertThat().body("data.iaqis.humidity", is(14))
                .assertThat().body("data.forecasts.pm1[0].iaqi", is("pm1"))
                .assertThat().body("data.forecasts.pm1[0].time", is("2021-05-10"))
                .assertThat().body("data.forecasts.pm1[0].avg", is(11))
                .assertThat().body("data.forecasts.pm1[0].max", is(20))
                .assertThat().body("data.forecasts.pm1[0].min", is(2));
    }
    @Test
    public void givenLisbonGeo_returnLisbon() {
        String lisbon = "Lisbon";
        AirQResponse another = new AirQResponse();
        BeanUtils.copyProperties(airQResponse, another);
        another.setCity(new City());
        another.getCity().setName(lisbon);
        another.getCity().setGeo(new Geo());
        another.getCity().getGeo().setLat(30.0);
        another.getCity().getGeo().setLng(40.0);
        Mockito.when(service.getAirQualityByGeoCoor(30.0, 40.0)).thenReturn(another);

        RestAssuredMockMvc.given()
                .get("/air_quality/json/geo/{lat}:{lgn}", "30.0", "40.0")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("success"))
                .assertThat().body("data.city.name", is(lisbon))
                .assertThat().body("data.city.geo.latitude", is("30.00"))
                .assertThat().body("data.city.geo.longitude", is("40.00"))
                .assertThat().body("data.dominentpol", is("humidity"))
                .assertThat().body("data.iaqis.pressure", is(10))
                .assertThat().body("data.iaqis.pm25", is(11))
                .assertThat().body("data.iaqis.temperature", is(12))
                .assertThat().body("data.iaqis.wind", is(13))
                .assertThat().body("data.iaqis.humidity", is(14))
                .assertThat().body("data.forecasts.pm1[0].iaqi", is("pm1"))
                .assertThat().body("data.forecasts.pm1[0].time", is("2021-05-10"))
                .assertThat().body("data.forecasts.pm1[0].avg", is(11))
                .assertThat().body("data.forecasts.pm1[0].max", is(20))
                .assertThat().body("data.forecasts.pm1[0].min", is(2));
    }


    @Test
    public void givenInvalidCity_thenNoDataForThisCity(){
        String invalid = "InvalidCity";
        Mockito.when(service.getAirQualityByLocation(invalid)).thenReturn(null);

        RestAssuredMockMvc.given()
                .get("/air_quality/json/location/{location}/", invalid)
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("error, no data found"));
    }

    @Test
    public void givenInvalidGeo_thenStatus4xx(){
        RestAssuredMockMvc.given()
                .get("/air_quality/json/geo/{lat}:{lgn}", "a", "b")
                .then().statusCode(400);
    }

    @Test
    public void getCacheStatistics() {

        Mockito.when(cache.getStatistics()).thenReturn(new Statistics(20, 5, 6, 5));

        RestAssuredMockMvc.given()
                .get("/air_quality/json/cache/statistics")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("msg", is("success"))
                .assertThat().body("data.size", is(20))
                .assertThat().body("data.hits", is(5))
                .assertThat().body("data.misses", is(6))
                .assertThat().body("data.collected", is(5));
    }
}
