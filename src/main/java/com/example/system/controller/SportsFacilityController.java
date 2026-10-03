package com.example.system.controller;

import com.example.system.common.PageResult;
import com.example.system.common.Result;
import com.example.system.entity.SportsFacility;
import com.example.system.service.SportsFacilityService;
import com.example.system.vo.FacilityStatisticsVO;
import com.example.system.vo.FacilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/facility")
public class SportsFacilityController {

    @Autowired
    private SportsFacilityService facilityService;

    // 添加设施
    @PostMapping("/add")
    public Result<Void> addFacility(@RequestBody SportsFacility facility) {
        facilityService.addFacility(facility);
        return Result.success();
    }

    // 更新设施
    @PostMapping("/update")
    public Result<Void> updateFacility(@RequestBody SportsFacility facility) {
        facilityService.updateFacility(facility);
        return Result.success();
    }

    // 删除设施
    @DeleteMapping("/{id}")
    public Result<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return Result.success();
    }

    // 获取设施列表
    @GetMapping("/list")
    public Result<List<SportsFacility>> listFacilities() {
        return Result.success(facilityService.getAllFacilities());
    }

    /**
     * 获取设施总数
     */
    @GetMapping("/count")
    public Result<Integer> getFacilityCount() {
        int count = facilityService.getTotalCount();
        return Result.success(count);
    }
    /**
     * 分页获取设施列表
     */
    @GetMapping("/page")
    public Result<PageResult<FacilityVO>> getFacilitiesByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<SportsFacility> facilities = facilityService.getFacilitiesByPage(pageNum, pageSize);
        int total = facilityService.getTotalCount();

        List<FacilityVO> voList = facilities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<FacilityVO> pageResult = new PageResult<>(pageNum, pageSize, total, voList);
        return Result.success(pageResult);
    }

    /**
     * 根据类型获取设施
     */
    @GetMapping("/type/{type}")
    public Result<List<FacilityVO>> getFacilitiesByType(@PathVariable Integer type) {
        List<SportsFacility> facilities = facilityService.getFacilitiesByType(type);
        List<FacilityVO> voList = facilities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 查询可预约的设施
     */
    @GetMapping("/available")
    public Result<List<FacilityVO>> getAvailableFacilities(
            @RequestParam String reservationDate,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        List<SportsFacility> facilities = facilityService.getAvailableFacilities(
                reservationDate, startTime, endTime);
        List<FacilityVO> voList = facilities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取设施详情
     */
    @GetMapping("/{id}")
    public Result<FacilityVO> getFacilityById(@PathVariable Long id) {
        SportsFacility facility = facilityService.getFacilityById(id);
        if (facility == null) {
            return Result.error("设施不存在");
        }
        return Result.success(convertToVO(facility));
    }

    /**
     * 获取设施统计信息（管理员）- 返回 FacilityStatisticsVO
     */
    @GetMapping("/statistics/{id}")
    public Result<FacilityStatisticsVO> getFacilityStatistics(
            @PathVariable Long id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {

        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole == 0) {
            return Result.error("权限不足");
        }

        FacilityStatisticsVO statistics = facilityService.getFacilityStatistics(id, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取所有设施统计（管理员）
     */
    @GetMapping("/all-statistics")
    public Result<List<Map<String, Object>>> getAllFacilitiesStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {

        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole == 0) {
            return Result.error("权限不足");
        }

        List<Map<String, Object>> statistics = facilityService.getAllFacilitiesStatistics(startDate, endDate);
        return Result.success(statistics);
    }







    /**
     * 转换方法：SportsFacility -> FacilityVO
     */
    private FacilityVO convertToVO(SportsFacility facility) {
        if (facility == null) return null;

        FacilityVO vo = new FacilityVO();
        BeanUtils.copyProperties(facility, vo);

        // 设施类型转换
        if (facility.getType() != null) {
            switch (facility.getType()) {
                case 1:
                    vo.setTypeText("篮球场");
                    break;
                case 2:
                    vo.setTypeText("足球场");
                    break;
                case 3:
                    vo.setTypeText("羽毛球场");
                    break;
                case 4:
                    vo.setTypeText("乒乓球场");
                    break;
                case 5:
                    vo.setTypeText("健身房");
                    break;
                case 6:
                    vo.setTypeText("游泳馆");
                    break;
                default:
                    vo.setTypeText("其他");
            }
        }

        // 状态转换
        if (facility.getStatus() != null) {
            vo.setStatusText(facility.getStatus() == 1 ? "正常" : "停用");
        }

        return vo;
    }
}