package cn.yam.sychatsdk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：
 * 日期：2024/9/19 下午4:46
 */
@AllArgsConstructor
@NoArgsConstructor
@Data

public class Message {
    private String role;
    private String content;
}