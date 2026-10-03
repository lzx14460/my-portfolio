package com.example.system.controller;

import com.example.system.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String API_KEY = "sk-mstejemleeuohjywqrgduoqdazmrcxxacognkogvcreybunj";
    private static final String MODEL = "Qwen/Qwen2.5-7B-Instruct";  //

    /**
     * 生成健康报告（调用 Qwen 模型）
     */
    @PostMapping("/health-report")
    public Result<String> generateHealthReport(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return Result.error("请输入内容");
        }

        try {
            String aiResponse = callQwenAPI(prompt);
            return Result.success(aiResponse);
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            // 返回本地建议而不是错误，提升用户体验
            return Result.success(generateLocalAdvice(prompt));
        }
    }

    private String callQwenAPI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        // 构建 messages 列表
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        // 构建请求体 - 使用正确的 MODEL 常量
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);  // 修正：使用 MODEL 常量
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    String content = message.get("content");
                    log.info("AI 响应成功，内容长度: {}", content.length());
                    return content;
                }
            }
        }

        log.error("API 响应格式异常: {}", response.getBody());
        return generateLocalAdvice(prompt);
    }

    /**
     * 生成本地建议
     */
    private String generateLocalAdvice(String prompt) {
        StringBuilder advice = new StringBuilder();
        advice.append("📋 健康评估报告\n");
        advice.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        if (prompt.contains("偏瘦")) {
            advice.append("【体型评估】偏瘦\n");
            advice.append("【运动建议】🏋️ 建议增加力量训练，每周3-4次，每次30-45分钟。\n");
            advice.append("【饮食建议】🍗 增加蛋白质摄入，多吃鸡蛋、牛奶、鱼肉、豆制品。\n");
            advice.append("【生活建议】😴 保证充足睡眠，规律作息，避免熬夜。\n");
        } else if (prompt.contains("正常")) {
            advice.append("【体型评估】标准\n");
            advice.append("【运动建议】🎯 保持现有运动习惯，每周150分钟中等强度有氧运动。\n");
            advice.append("【饮食建议】🥗 保持均衡饮食，多吃蔬菜水果，控制油盐。\n");
            advice.append("【生活建议】💪 继续保持健康生活方式，定期监测体重。\n");
        } else if (prompt.contains("超重")) {
            advice.append("【体型评估】超重\n");
            advice.append("【运动建议】🔥 增加有氧运动频率，每周5次以上，每次30-60分钟。\n");
            advice.append("【饮食建议】🥦 控制高热量食物摄入，增加蔬菜和膳食纤维。\n");
            advice.append("【生活建议】📝 记录每日饮食，保持运动打卡，循序渐进。\n");
        } else if (prompt.contains("肥胖")) {
            advice.append("【体型评估】肥胖\n");
            advice.append("【运动建议】🚶 从低强度运动开始，如快走、游泳，每周3-5次。\n");
            advice.append("【饮食建议】🥑 咨询营养师，制定科学减重计划，控制总热量。\n");
            advice.append("【生活建议】❤️ 循序渐进，坚持就是胜利！关注健康指标变化。\n");
        } else {
            advice.append("【体型评估】待完善\n");
            advice.append("【运动建议】🏃 建议定期测量身高体重，关注BMI指数变化。\n");
            advice.append("【饮食建议】🍎 保持均衡饮食，适量运动，健康生活！\n");
            advice.append("【生活建议】📊 建议连续记录健康数据，获取更精准的分析。\n");
        }

        advice.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        advice.append("💡 温馨提示：本报告仅供参考，如需更详细的建议，请咨询专业医生或运动教练。");

        return advice.toString();
    }


}