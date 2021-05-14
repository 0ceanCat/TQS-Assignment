package tqs.assignment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @author wy
 * @date 2021/5/13 12:23
 */
@Data
@NoArgsConstructor
public class JsonResponse implements Serializable {
    private String msg;
    private ResponseData data;

    public JsonResponse(ResponseData data){
        if (data == null){
            this.msg = "error, no data found";
        }else {
            this.msg = "success";
        }

        this.data = data;
    }

    public JsonResponse(String msg){
        this.msg = msg;
        this.data = null;
    }
}
