package tqs.assignment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * @author wy
 * @date 2021/4/24 21:07
 */
@Data
@NoArgsConstructor
public class Record {
    private LocalDateTime creation = LocalDateTime.now();
    private LocalDateTime liveUtil;
    private AirQResponse airQResponse;

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(liveUtil);
    }
}
