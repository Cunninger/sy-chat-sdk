package cn.yam.sychatsdk;

/**
 * 功能：
 * 日期：2024/9/19 下午4:44
 */

import cn.yam.sychatsdk.config.SyChatProperties;

import cn.yam.sychatsdk.model.RequestPayload;

import cn.yam.sychatsdk.model.Message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
@Component
public class SyClient {

    private final SyChatProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public SyClient(SyChatProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 发送消息并获取回复
     *
     * @param content 消息内容
     * @return 回复内容
     * @throws Exception 异常
     */
    public String chat(String content) throws Exception {
        return chat(content, null, null, null);
    }

    /**
     * 发送消息并获取回复，允许覆盖配置
     *
     * @param content   消息内容
     * @param model     模型名称
     * @param maxTokens 最大生成长度
     * @param stream    是否流式输出
     * @return 回复内容
     * @throws Exception 异常
     */
    public String chat(String content, String model, Integer maxTokens, Boolean stream) throws Exception {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        // 构建消息
        Message message = new Message("user", content);

        // 构建请求载荷
        RequestPayload payload = new RequestPayload();
        payload.setMessages(Collections.singletonList(message));
        payload.setModel(model != null ? model : properties.getModel());
        payload.setMax_tokens(maxTokens != null ? maxTokens : properties.getMaxTokens());
        payload.setStream(stream != null ? stream : properties.getStream());

        // 将请求载荷转换为 JSON 字符串
        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.exchange(
                properties.getBaseUrl(),
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        String result = root.path("choices").get(0).path("message").path("content").asText();
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println(result);
            return result;
        } else {
            throw new RuntimeException("请求失败，状态码：" + response.getStatusCodeValue());
        }
    }
}
