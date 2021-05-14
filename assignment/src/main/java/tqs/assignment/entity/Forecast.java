package tqs.assignment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wy
 * @date 2021/4/26 14:30
 */
@Data
@NoArgsConstructor
public class Forecast {
    private String iaqi;
    private Integer avg;
    private String time;
    private Integer max;
    private Integer min;
}
