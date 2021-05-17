package tqs.assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import tqs.assignment.component.Cache;
import tqs.assignment.entity.City;
import tqs.assignment.entity.AirQResponse;
import tqs.assignment.service.impl.AirQualityServiceImpl;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * @author wy
 * @date 2021/4/28 10:34
 */
@ExtendWith(MockitoExtension.class)
public class AQService_WithMock {
    private static final String URL = "https://api.waqi.info/feed/";

    private static final String JOIN_STR = "?token=";

    private static final String TOKEN = "6934d1d173fc8dd18ee0f5e2be89497fd7e1995f";

    @Mock(lenient = true)
    private Cache cache;

    @Mock(lenient = true)
    private RestTemplate restTemplate;

    @InjectMocks
    private AirQualityServiceImpl service;

    private AirQResponse airQResponse;

    @BeforeEach
    public void setUp() {
        String location = "Aveiro";
        airQResponse = new AirQResponse();
        City city = new City();
        city.setName(location);

        airQResponse.setCity(city);
        airQResponse.setDominentpol("pm3");
        airQResponse.setIaqis(Map.of("pm1", 10, "pm2", 11, "pm3", 12));

        // when get Aveiro's air quality, service will find Aveiro's air info in cache
        Mockito.when(cache.get(location)).thenReturn(airQResponse);

        // when get Porto's air quality, service should parse a Map object and build a response
        String url = getUrl("porto");
        Map<String, Object> portoMap = new HashMap<>();
        portoMap.put("status", "success");
        portoMap.put("data", Map.of("city", Map.of("name", "porto")));
        Mockito.when(restTemplate
                .getForObject(url, Map.class)).thenReturn(portoMap);


        url = getUrl("invalid");
        Map<String, Object> invalidMap = new HashMap<>();
        invalidMap.put("status", "error");
        invalidMap.put("another", new LinkedHashMap<>());
        Mockito.when(restTemplate
                .getForObject(url, Map.class)).thenReturn(invalidMap);

    }

    @Test
    void givenAveiro_fromCache() {
        AirQResponse aveiro = service.getAirQualityByLocation("Aveiro");
        assertEquals("Aveiro", aveiro.getCity().getName());
        assertEquals("pm3", aveiro.getDominentpol());
        assertEquals(12, aveiro.getIaqis().get("pm3"));

        verify(cache, VerificationModeFactory.times(1)).get("Aveiro");
        verify(cache, VerificationModeFactory.times(0)).put(anyString(), any());
    }

    @Test
    void givenValidCity_thenIsPutInCache() {
        AirQResponse porto = service.getAirQualityByLocation("porto");

        assertEquals("porto", porto.getCity().getName());
        assertNull(porto.getForecasts());
        assertNull(porto.getIaqis());

        verify(cache, VerificationModeFactory.times(1)).get("porto");
        verify(cache, VerificationModeFactory.times(1)).put("porto", porto);
    }

    @Test
    void givenInvalidCity_thenReturnNull() {
        String url = "invalid";
        AirQResponse response = service.getAirQualityByLocation(url);

        assertNull(response);
        verify(cache, VerificationModeFactory.times(1)).get(url);
        verify(cache, VerificationModeFactory.times(1)).put(url, null);
    }

    private String getUrl(String city){
        return URL + city + "/" + JOIN_STR + TOKEN;
    }

}


