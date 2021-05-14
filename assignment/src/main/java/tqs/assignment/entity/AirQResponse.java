package tqs.assignment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wy
 * @date 2021/4/26 14:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirQResponse implements ResponseData{
    private static final Map<String, String> CONVERSION = Map.of("h", "humidity",
                                                        "p", "pressure",
                                                        "t", "temperature",
                                                        "w", "wind");
    private City city;
    private String dominentpol;
    private Map<String, Number> iaqis;
    private Map<String, List<Forecast>> forecasts;

    public void setDominentpol(String d){
        this.dominentpol = CONVERSION.getOrDefault(d, d);
    }
    public void setIaqis(Map<String, Number> iaqis){
        Map<String, Number> temp = new HashMap<>();
        iaqis.forEach((k,v)-> temp.put(CONVERSION.getOrDefault(k, k), v));
        this.iaqis = temp;
    }

    public void setForecasts(Map<String, List<Forecast>> forecasts){
        Map<String, List<Forecast>> temp = new HashMap<>();
        forecasts.forEach((k,v)-> temp.put(CONVERSION.getOrDefault(k, k), v));
        this.forecasts = temp;
    }
}
