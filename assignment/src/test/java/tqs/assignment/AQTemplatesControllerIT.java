package tqs.assignment;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tqs.assignment.component.Cache;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import tqs.assignment.entity.Statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author wy
 * @date 2021/5/8 21:51
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AssignmentApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class AQTemplatesControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Cache cache;

    @Test
    public void index() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @Order(1)
    public void givenPorto_returnPorto_CacheSize1() throws Exception {
        mvc.perform(get("/air_quality/location/{location}", "Porto"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[@id='city_name']")
                        .string("Sobreiras-Lordelo do Ouro, Porto, Portugal"))
                .andExpect(xpath("//h1[@id='city_geo']")
                        .string("Geo: 41.15, -8.66"));

        assertEquals(1, cache.size());
    }

    @Test
    @Order(2)
    public void givenInvalidCity_thenNoDataFoundMisses2Hit1() throws Exception {
        String invalid = "abcde";
        mvc.perform(get("/air_quality/location/{location}", invalid))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[contains(@class, 'invalid_city_country')]").string("No air quality data for city/country:  " + invalid +" "));

        // misses is 2 because we access the cache through an AirQualityService
        // When the Service receives a request, the first thing it does is cache.get('xxx')
        // to see if there already have a corresponded cached data. if not, misses is incremented, else
        // hits is incremented.
        // So, misses was incremented to 1 in the test before, now it is 2
        assertEquals(2, cache.misses());
    }

    @Test
    @Order(3)
    public void get2CachedCities_thenHit2() throws Exception {

        mvc.perform(get("/air_quality/location/{location}", "Porto"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[@id='city_name']").string("Sobreiras-Lordelo do Ouro, Porto, Portugal"))
                .andExpect(xpath("//h1[@id='city_geo']").string("Geo: 41.15, -8.66"));

        mvc.perform(get("/air_quality/location/{location}", "abcde"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[contains(@class, 'invalid_city_country')]").string("No air quality data for city/country:  abcde "));


        assertEquals(2, cache.hits());
    }

    @Test
    @Order(4)
    public void givenPortugalGeo_returnPortugal_thenSize3() throws Exception {
        mvc.perform(get("/air_quality/geo/{lat}:{lgn}", "32.65", "-16.92"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(xpath("//h1[@id='city_name']").string("São Gonçalo, Funchal, Portugal"))
                .andExpect(xpath("//h1[@id='city_geo']").string("Geo: 32.65, -16.92"));

        assertEquals(3, cache.size());
    }

    @Test
    @Order(5)
    public void givenInvalidGeo_then4xx() throws Exception {
        mvc.perform(get("/air_quality/geo/{lat}:{lgn}", "abc", "bbbb"))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @Order(6)
    public void getStatistics() throws Exception {
        Statistics statistics = cache.getStatistics();

        mvc.perform(get("/air_quality/cache/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[1]").string(statistics.getSize().toString()))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[2]").string(statistics.getHits().toString()))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[3]").string(statistics.getMisses().toString()))
                .andExpect(xpath("//table[contains(@class, 'statistic_tbl')]//td[4]").string(statistics.getCollected().toString()));

    }
}
