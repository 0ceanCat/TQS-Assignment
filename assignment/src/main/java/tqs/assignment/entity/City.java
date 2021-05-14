package tqs.assignment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wy
 * @date 2021/4/26 12:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    private String name;
    private Geo geo;
}
