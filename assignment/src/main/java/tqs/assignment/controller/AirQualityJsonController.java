package tqs.assignment.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.assignment.component.Cache;
import tqs.assignment.entity.JsonResponse;
import tqs.assignment.service.AirQualityService;

/**
 * @author wy
 * @date 2021/5/9 11:50
 */
@RestController
@RequestMapping("/air_quality/json/")
public class AirQualityJsonController {

    @Autowired
    AirQualityService service;

    @Autowired
    Cache cache;

    @ApiOperation("Get air quality in json format by city or country")
    @GetMapping("/location/{location}")
    public JsonResponse getAirQualityByLocation(@PathVariable(value = "location") String city) {
        return new JsonResponse(service.getAirQualityByLocation(city));
    }

    @ApiOperation("Get air quality in json format by the given geo coordinates")
    @GetMapping("/geo/{lat}:{lng}")
    public JsonResponse getAirQualityByGeoCoor(@PathVariable(value = "lat") Double lat,
                                               @PathVariable(value = "lng") Double lng) {
        if (lat == null || lng == null) return null;

        return new JsonResponse(service.getAirQualityByGeoCoor(lat, lng));
    }

    @ApiOperation("Get cache statistics in json format: cache's size, hits, misses and number of data were collected")
    @GetMapping("/cache/statistics")
    public JsonResponse getStatistics(){
        return new JsonResponse(cache.getStatistics());
    }
}
