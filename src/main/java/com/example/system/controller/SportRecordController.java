package com.example.system.controller;

import com.example.system.common.Result;
import com.example.system.dto.SportRecordDTO;
import com.example.system.service.SportRecordService;
import com.example.system.vo.SportRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sport-record")
public class SportRecordController {

    @Autowired
    private SportRecordService sportRecordService;

    @PostMapping("/add")
    public Result<SportRecordVO> addSportRecord(@Valid @RequestBody SportRecordDTO dto, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        try {
            SportRecordVO vo = sportRecordService.addSportRecord(userId, dto);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("添加运动记录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-list")
    public Result<List<SportRecordVO>> getMySportRecords(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(sportRecordService.getSportRecords(userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSportRecord(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        try {
            boolean result = sportRecordService.deleteSportRecord(id, userId);
            return result ? Result.success() : Result.error("记录不存在");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}