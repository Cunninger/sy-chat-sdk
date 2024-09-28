package cn.yam.sychatsdk.utils;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TokensCountUtil {

    public static int countTokens(String filePath) throws Exception {
        // 创建实例
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        // 指定 CL100K_BASE 编码
        Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);
        // 进行编码
        File file = new File(filePath);
        // 把文件内容转为字符串
        String filestring = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        List<Integer> encoded = enc.encode(filestring);
        // 返回编码后的集合大小（也就是token数）
        return encoded.size();
    }
}