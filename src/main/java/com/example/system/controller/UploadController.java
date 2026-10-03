package com.example.system.controller;

import com.example.system.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url}")
    private String accessUrl;

    /**
     * 上传图片文件（仅场馆管理员和系统管理员可用）
     * 支持 JPG、PNG、JPEG、GIF 格式，最大 5MB
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        log.info("=== 开始处理图片上传 ===");

        // 修改权限：允许场馆管理员(1) 和 系统管理员(2)
        Integer userRole = (Integer) session.getAttribute("userRole");
        log.info("当前用户角色: {}", userRole);

        if (userRole == null || (userRole != 1 && userRole != 2)) {
            return Result.error("权限不足，需要场馆管理员或系统管理员权限");
        }

        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            return Result.error("只能上传图片文件（JPG、PNG、JPEG、GIF）");
        }

        // 检查文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        try {
            // 生成新的文件名
            String newFileName = generateFileName(originalFilename);

            // 创建日期目录
            String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            // 统一用 / 拼接路径
            String fullUploadPath = uploadPath.endsWith(File.separator)
                    ? uploadPath + dateDir
                    : uploadPath + File.separator + dateDir;
            // 或者更安全的方式：
            // String fullUploadPath = Paths.get(uploadPath, dateDir).toString();

            // 打印实际存储路径，方便排查
            log.info("文件将保存到: {}", fullUploadPath);
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                log.info("创建目录结果: {}", created);
            }

            // 保存文件
            File targetFile = new File(uploadDir, newFileName);
            file.transferTo(targetFile);
            log.info("文件保存成功: {}", targetFile.getAbsolutePath());

            // 修改：返回相对路径而不是完整URL
            // 这样前端会自动使用当前域名
            String imageUrl = "/api/uploads/" + dateDir + "/" + newFileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", imageUrl);
            result.put("filename", newFileName);

            log.info("图片上传成功，访问路径: {}", imageUrl);
            return Result.success(result);

        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 判断文件是否为图片类型
     */
    private boolean isImageFile(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".jpg") ||
                lowerCase.endsWith(".jpeg") ||
                lowerCase.endsWith(".png") ||
                lowerCase.endsWith(".gif");
    }

    /**
     * 生成唯一文件名（UUID + 原文件扩展名）
     */
    private String generateFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}