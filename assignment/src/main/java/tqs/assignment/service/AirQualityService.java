package tqs.assignment.service;

import tqs.assignment.entity.AirQResponse;

/**
 * @author wy
 * @date 2021/4/28 12:09
 */
public interface AirQualityService {
    AirQResponse getAirQualityByLocation(String city);
    AirQResponse getAirQualityByGeoCoor(Double lat, Double lng);
}
