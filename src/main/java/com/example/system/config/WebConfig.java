package com.example.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 静态资源配置（映射上传文件访问路径）
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 构造函数：启动时执行，打印日志说明被扫描加载
    public WebConfig() {
        log.info("========================================");
        log.info("WebConfig 配置类已被 Spring 扫描并加载");
        log.info("========================================");
    }

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 处理路径分隔符（兼容Windows/Linux）
        String physicalPath = uploadPath.replace("\\", "/");
        // 2. 确保路径以 / 结尾
        if (!physicalPath.endsWith("/")) {
            physicalPath += "/";
        }
        // 3. 拼接 file: 协议（必须正确格式：file:/D:/xxx/ 或 file:/home/xxx/）
        String resourceLocation = "file:/" + physicalPath;
        // 修复：如果是Windows路径，避免出现 file://D:/xxx（双斜杠）
        if (resourceLocation.startsWith("file:///")) {
            resourceLocation = resourceLocation.replace("file:///", "file:/");
        }

        log.info("========================================");
        log.info("静态资源映射配置:");
        log.info("  请求路径: /uploads/**");
        log.info("  映射到物理路径: {}", resourceLocation);
        log.info("  实际文件路径示例: {}{}", physicalPath, "2026/04/07/xxx.jpg");
        log.info("========================================");

        // 关键：addResourceLocations 必须是目录，且以 / 结尾
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                // 可选：添加缓存控制（开发环境关闭缓存）
                .setCachePeriod(0);
    }
}