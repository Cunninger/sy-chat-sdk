
# SyChat SDK

## 概述

SyChat SDK 是一个高性能、可扩展的聊天机器人开发工具包，旨在简化与聊天模型的集成。通过提供简洁的API和灵活的配置选项，开发者可以轻松地构建和部署智能聊天应用。

## 特性

- **高性能**：基于Spring Boot，提供快速响应和高并发处理能力。
- **灵活配置**：支持通过`application.properties`或`application.yml`文件进行配置，满足不同环境的需求。
- **易于集成**：提供简单易用的API，支持快速集成到现有项目中。
- **扩展性强**：支持自定义消息处理逻辑和模型配置，满足复杂业务需求。

## 快速开始

### 环境要求

- JDK 8或更高版本
- Maven 3.6.0或更高版本


### 配置
1. 在`pom.xml`文件中添加以下依赖：
```xml
<dependency>
   <groupId>io.gitee.a-little-zhu</groupId>
   <artifactId>sy-chat-sdk</artifactId>
   <version>0.0.3</version>
</dependency>
```

2. 在`src/main/resources/application.properties`文件中配置以下属性：
```yaml
sy:
   chat:
      api-key: 'YBHTRrFtcl0aJOFBPDgpebYwxXWcJGRUplm1PVMeCULc'
      base-url: 'https://ollama.yamazing.cn/v1/chat/completions'
      model: 'llama3-8b-8192'
      max-tokens: 4096
      stream: false
```
3. 在项目启动类中添加`@EnableConfigurationProperties`注解：

```java
@EnableConfigurationProperties(SyChatProperties.class)// 扫描读取yaml配置
```
- 例如
```java
@EnableConfigurationProperties(SyChatProperties.class)
@SpringBootApplication
public class ApiSdkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiSdkApplication.class, args);
    }

}
```
4. 如果需要本地部署此SDK作二次开发，请自行添加application.yml文件，并补充相关模型配置。
### 使用示例

```java
/**
 * @author: wsy
 */
@RestController
@RequestMapping("/sdk")
public class SdkController {
   @Resource
   private SyChatProperties syChatProperties;// 必须引入
   @GetMapping("/chat")
   public void chat(HttpServletResponse response,String msg) throws Exception {
      SyClient client = new SyClient(syChatProperties);
      String s = client.chat(msg);
      System.out.println(response);
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().write(s);
   }

}
```
- 访问端口: `http://<ip>:<port>/sdk/chat?msg=中文回答，现在入行Java咋样？`
#### 注意事项
- 因为逆向的是ollama模型，默认回复为英文，如果需要模型回复中文，需要指定语言，如上所示。
## 高级配置

### 自定义消息处理

您可以通过扩展`SyClient`类来自定义消息处理逻辑，以满足特定业务需求。

### 多环境支持

通过在`settings.xml`中配置Maven Profile，支持多环境配置：

```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation>
            <property>
                <name>env</name>
                <value>dev</value>
            </property>
        </activation>
        <properties>
            <sy.chat.baseUrl>https://dev.api.yourchatmodel.com</sy.chat.baseUrl>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <activation>
            <property>
                <name>env</name>
                <value>prod</value>
            </property>
        </activation>
        <properties>
            <sy.chat.baseUrl>https://api.yourchatmodel.com</sy.chat.baseUrl>
        </properties>
    </profile>
</profiles>
```

## 贡献

欢迎提交Issue和Pull Request来帮助我们改进SyChat SDK。请确保在提交之前阅读我们的[贡献指南](CONTRIBUTING.md)。

## 许可证

本项目采用MIT许可证，详情请参阅[LICENSE](LICENSE)文件。
