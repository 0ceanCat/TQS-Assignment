package tqs.assignment.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import tqs.assignment.component.Cache;
import tqs.assignment.service.AirQualityService;
/**
 * @author wy
 * @date 2021/5/9 11:50
 */
@Controller
@RequestMapping("/air_quality/")
public class AirQualityTemplatesController {

    @Autowired
    AirQualityService service;

    @Autowired
    Cache cache;

    @GetMapping( "/")
    public String index() {
        return "index";
    }

    @ApiOperation("Get air quality by city or country")
    @GetMapping("/location/{location}")
    public String getAirQualityByLocation(@PathVariable(value = "location") String city,
                                          Model model) {
        model.addAttribute("aqi", service.getAirQualityByLocation(city));
        model.addAttribute("city", city);
        return "details";
    }

    @ApiOperation("Get air quality by the given geo coordinates")
    @GetMapping("/geo/{lat}:{lng}")
    public String getAirQualityByGeoCoor(@PathVariable(value = "lat") Double lat,
                                         @PathVariable(value = "lng") Double lng,
                                         Model model) {
        if (lat == null || lng == null) return null;

        model.addAttribute("aqi", service.getAirQualityByGeoCoor(lat, lng));

        return "details";
    }

    @ApiOperation("Get cache statistics: cache's size, hits, misses and number of data were collected")
    @GetMapping("/cache/statistics")
    public String getStatistics(Model model){
        model.addAttribute("statistics", cache.getStatistics());
        return "statistics";
    }
}
