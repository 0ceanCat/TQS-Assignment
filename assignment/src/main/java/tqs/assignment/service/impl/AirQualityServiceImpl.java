package tqs.assignment.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tqs.assignment.component.Cache;
import tqs.assignment.entity.City;
import tqs.assignment.entity.Forecast;
import tqs.assignment.entity.Geo;
import tqs.assignment.entity.AirQResponse;
import tqs.assignment.service.AirQualityService;
import java.util.*;

/**
 * @author wy
 * @date 2021/4/28 10:35
 */
@Service
public class AirQualityServiceImpl implements AirQualityService {
    private static final String URL = "https://api.waqi.info/feed/";
    private static final String JOIN_STR = "?token=";
    private static final String TOKEN = "6934d1d173fc8dd18ee0f5e2be89497fd7e1995f";

    @Autowired
    private Cache cache;

    @Autowired
    private RestTemplate restTemplate;

    public AirQResponse getAirQualityByLocation(String location) {
        if (location == null) return null;

        AirQResponse cached = cache.get(location);

        if (cached != null) return cached;

        String url = URL + location + "/" + JOIN_STR + TOKEN;

        return doRequestGetResponse(url, location);
    }

    @Override
    public AirQResponse getAirQualityByGeoCoor(Double lat, Double lng) {

        String param = lat + ":" + lng;

        AirQResponse cached = cache.get(param);

        if (cached != null) return cached;

        String url = URL + "geo:" + lat + ";" + lng + "/" + JOIN_STR + TOKEN;

        return doRequestGetResponse(url, param);
    }

    private AirQResponse doRequestGetResponse(String url, String param){
        Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
        if (!verifyStatus(resp, param))
            return null;

        AirQResponse entity = new AirQResponse();
        fill(entity, resp);

        cache.put(param, entity);
        return entity;
    }

    private void fill(AirQResponse entity, Map<String, Object> resp) {
        if (resp == null) return;

        Map<String, Object> data = (Map) resp.get("data");
        if (data == null) return;

        entity.setDominentpol((String) data.get("dominentpol"));

        Map<String, Object> cityInfo = (Map) data.get("city");
        if (cityInfo == null) return;

        City city = new City();
        Geo geo = new Geo();
        city.setGeo(geo);
        entity.setCity(city);
        city.setName((String) cityInfo.get("name"));

        List<Double> latLng = (List<Double>) cityInfo.get("geo");
        if (latLng == null) return;

        geo.setLat(latLng.get(0));
        geo.setLng(latLng.get(1));

        Map<String, Map<String, Number>> iaqi = (Map) data.get("iaqi");
        if (iaqi == null) return;

        Map<String, Number> iaqiMap = new HashMap<>();
        iaqi.forEach((k,v)-> iaqiMap.put(k, v.get("v")));
        entity.setIaqis(iaqiMap);

        Map<String, Object> forecast = (Map) data.get("forecast");
        if (forecast == null) return;

        Map<String, List<Map<String, Object>>> daily = (Map) forecast.get("daily");
        if (daily == null) return;

        Map<String, List<Forecast>> forecastMap = new HashMap<>();
        daily.forEach((k,v)->{
            v.forEach(map -> {
                final Map<String, Object> l = map;

                Forecast f = new Forecast();
                f.setIaqi(k);
                f.setMax( (Integer)l.get("max") );
                f.setMin( (Integer)l.get("min") );
                f.setTime( (String) l.get("day") );
                f.setAvg( (Integer)l.get("avg") );
                forecastMap.compute(k, (key, value)->{
                    if (value == null) return new ArrayList<>();
                    value.add(f);
                    return value;
                });
            });
        });

        entity.setForecasts(forecastMap);
    }

    private boolean verifyStatus(Map<String, Object> resp, String param){
        if (resp == null || "error".equals(resp.get("status"))){
            cache.put(param, null);
            return false;
        }
        return true;
    }
}
