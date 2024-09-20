package cn.yam.sychatsdk.config;

/**
 * 功能：
 * 日期：2024/9/19 下午4:45
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "sy.chat")
@AllArgsConstructor
@NoArgsConstructor
public class SyChatProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
    private Integer maxTokens;
    private Boolean stream;

}
