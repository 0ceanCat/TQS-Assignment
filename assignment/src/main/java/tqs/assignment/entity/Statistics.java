package tqs.assignment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wy
 * @date 2021/5/7 21:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistics implements ResponseData{
    private Integer size;
    private Integer hits;
    private Integer misses;
    private Integer collected;
}
