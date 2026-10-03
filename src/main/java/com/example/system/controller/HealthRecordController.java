package com.example.system.controller;

import com.example.system.common.Result;
import com.example.system.dto.HealthRecordDTO;
import com.example.system.service.HealthRecordService;
import com.example.system.vo.HealthRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    /**
     * 保存健康数据记录
     */
    @PostMapping("/record")
    public Result<Void> saveHealthRecord(@Valid @RequestBody HealthRecordDTO dto,
                                         HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        try {
            // 解析记录日期
            LocalDate recordDate;
            if (dto.getRecordDate() != null && !dto.getRecordDate().isEmpty()) {
                recordDate = LocalDate.parse(dto.getRecordDate());
            } else {
                recordDate = LocalDate.now();
            }

            healthRecordService.saveRecord(userId, recordDate, dto.getHeight(), dto.getWeight());
            log.info("保存健康数据成功: userId={}, date={}", userId, recordDate);
            return Result.success();
        } catch (Exception e) {
            log.error("保存健康数据失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户健康数据历史
     */
    @GetMapping("/records")
    public Result<List<HealthRecordVO>> getHealthRecords(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<HealthRecordVO> records = healthRecordService.getRecordsByUserId(userId);
        return Result.success(records);
    }
}