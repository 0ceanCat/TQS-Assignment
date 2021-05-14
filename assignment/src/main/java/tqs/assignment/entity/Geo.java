package tqs.assignment.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author wy
 * @date 2021/4/28 12:04
 */
@Getter
@NoArgsConstructor
public class Geo{
    private String latitude;
    private String longitude;

    public void setLat(Double lat) {
        if (lat < -90 || lat > 90){
            throw new IllegalArgumentException("Latitude must be a double between -90 and 90");
        }
        this.latitude = String.format("%.2f", lat);
    }

    public void setLng(Double lng) {
        if (lng < -180 || lng > 180){
            throw new IllegalArgumentException("Longitude must be a double between -180 and 180");
        }
        this.longitude = String.format("%.2f", lng);
    }

    @Override
    public String toString() {
        return "Geo: " + latitude + ", " + longitude;
    }
}
