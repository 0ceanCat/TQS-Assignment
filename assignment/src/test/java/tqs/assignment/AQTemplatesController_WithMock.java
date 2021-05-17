package tqs.assignment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.assignment.component.Cache;
import tqs.assignment.controller.AirQualityTemplatesController;
import tqs.assignment.entity.*;
import tqs.assignment.service.AirQualityService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author wy
 * @date 2021/5/8 17:52
 */
@WebMvcTest(AirQualityTemplatesController.class)
public class AQTemplatesController_WithMock {
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

    @Test
    public void givenAveiro_returnAveiro() throws Exception {

        Mockito.when(service.getAirQualityByLocation("Aveiro")).thenReturn(airQResponse);

        mvc.perform(get("/air_quality/location/{location}", "Aveiro"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[1]").string("Tqs Air Quality"))
                .andExpect(xpath("//h1[@id='city_name']").string("Aveiro"))
                .andExpect(xpath("//h1[@id='city_geo']").string("Geo: 10.00, 20.00"))
                    // iaqis ↓
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[1]").string("10")) // p
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[2]").string("11")) // pm25
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[3]").string("12")) // t
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[4]").string("13")) // w
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[5]").string("14")) // h
                    // forecast ↓
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[1]").string("2021-05-10")) // time
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[2]").string("20")) // max
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[3]").string("2")) // min
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[4]").string("11")); // avg
    }

    @Test
    public void givenPortoGeo_returnPorto() throws Exception {

        AirQResponse another = new AirQResponse();
        BeanUtils.copyProperties(airQResponse, another);
        another.setCity(new City());
        another.getCity().setName("Porto");
        another.getCity().setGeo(new Geo());
        another.getCity().getGeo().setLat(30.0);
        another.getCity().getGeo().setLng(40.0);
        Mockito.when(service.getAirQualityByGeoCoor(30.0, 40.0)).thenReturn(another);

        mvc.perform(get("/air_quality/geo/{lat}:{lgn}", "30.0", "40.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[1]").string("Tqs Air Quality"))
                .andExpect(xpath("//h1[@id='city_name']").string("Porto"))
                .andExpect(xpath("//h1[@id='city_geo']").string("Geo: 30.00, 40.00"))
                // iaqis ↓
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[1]").string("10")) // p
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[2]").string("11")) // pm25
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[3]").string("12")) // t
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[4]").string("13")) // w
                .andExpect(xpath("//table[contains(@class, 'iaqis-tbl')]//td[5]").string("14")) // h
                // forecast ↓
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[1]").string("2021-05-10")) // time
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[2]").string("20")) // max
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[3]").string("2")) // min
                .andExpect(xpath("//table[contains(@class, 'forecast-tbl')]//td[4]").string("11")); // avg
    }


    @Test
    public void givenInvalidCity_thenNoDataForThisCity() throws Exception {
        String invalid = "InvalidCity";
        Mockito.when(service.getAirQualityByLocation(invalid)).thenReturn(null);

        mvc.perform(get("/air_quality/location/{location}", invalid))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[contains(@class, 'invalid_city_country')]")
                        .string("No air quality data for city/country:  "
                                + invalid+ " "));
    }

    @Test
    public void givenInvalidGeo_then4xx() throws Exception {
        mvc.perform(get("/geo/{lat}:{lgn}", "abc", "def"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getCacheStatistics() throws Exception {

        Mockito.when(cache.getStatistics()).thenReturn(new Statistics(20, 5, 6, 5));

        mvc.perform(get("/air_quality/cache/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[1]").string("20"))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[2]").string("5"))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[3]").string("6"))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[4]").string("5"));
    }

}
