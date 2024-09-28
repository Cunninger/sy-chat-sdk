package cn.yam.sychatsdk;

import cn.yam.sychatsdk.config.SyChatProperties;
import cn.yam.sychatsdk.model.RequestPayload;
import cn.yam.sychatsdk.model.Message;
import cn.yam.sychatsdk.utils.TokensCountUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SyClient {
    private final SyChatProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Random random;

    // 使用线程安全的列表来存储消息历史
    private final List<Message> messageHistory;

    public static final String MESSAGE_HISTORY_FILE = "message-history.json";
    @Autowired
    public SyClient(SyChatProperties syChatProperties) throws IOException {
        this.properties = syChatProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
        this.messageHistory = new CopyOnWriteArrayList<>();
        loadMessageHistoryFromFile(MESSAGE_HISTORY_FILE);
    }

    private String getRandomApiKey() {
        List<String> apiKeys = properties.getApiKeys();
        return apiKeys.get(random.nextInt(apiKeys.size()));
    }

    /**
     * 从文件加载消息历史
     * @param filePath 文件路径
     * @throws IOException 异常
     */
    public void loadMessageHistoryFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            // 如果文件不存在，则创建一个空文件
            Files.createFile(Paths.get(filePath));
            objectMapper.writeValue(file, new Message[0]);
        }
        if (file.length() > 0) {
            Message[] messages = objectMapper.readValue(file, Message[].class);
            messageHistory.clear();
            Collections.addAll(messageHistory, messages);
        }
    }

    /**
     * 保存消息历史到文件
     * @param filePath
     * @throws IOException
     */

    public void saveMessageHistoryToFile(String filePath) throws IOException {
        File file = new File(filePath);
        objectMapper.writeValue(file, messageHistory);
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
        // 将用户消息添加到消息历史中
        Message userMessage = new Message("user", content);
        messageHistory.add(userMessage);
        while (TokensCountUtil.countTokens(MESSAGE_HISTORY_FILE) >new SyClient(properties).properties.getMaxTokens()) {
            messageHistory.remove(0);
            // 同步更新到文件
            saveMessageHistoryToFile(MESSAGE_HISTORY_FILE);
        }// 限制消息历史记录的长度

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getRandomApiKey());

        // 构建请求载荷
        RequestPayload payload = new RequestPayload();
        payload.setMessages(messageHistory);
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
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choice = root.path("choices").get(0);
        String result = choice.path("message").path("content").asText();

        if (response.getStatusCode().is2xxSuccessful()) {
            // 将助手的回复添加到消息历史中
            Message assistantMessage = new Message("assistant", result);
            messageHistory.add(assistantMessage);
            saveMessageHistoryToFile(MESSAGE_HISTORY_FILE);
            return result;
        } else {
            throw new RuntimeException("请求失败，状态码：" + response.getStatusCodeValue());
        }
    }

    /**
     * 清除消息历史记录
     */
    public void clearHistory() {
        messageHistory.clear();
    }

    /**
     * 获取当前的消息历史记录
     *
     * @return 消息历史列表
     */
    public List<Message> getMessageHistory() {
        return Collections.unmodifiableList(messageHistory);
    }
}
