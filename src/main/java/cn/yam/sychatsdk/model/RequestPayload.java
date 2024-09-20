package cn.yam.sychatsdk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 功能：
 * 日期：2024/9/19 下午4:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class RequestPayload {
    private List<Message> messages;
    private String model;
    private Integer max_tokens;
    private Boolean stream;
}