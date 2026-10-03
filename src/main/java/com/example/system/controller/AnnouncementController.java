package com.example.system.controller;

import com.example.system.common.PageResult;
import com.example.system.common.Result;
import com.example.system.entity.Announcement;
import com.example.system.service.AnnouncementService;
import com.example.system.vo.AnnouncementVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取当前有效公告（所有人可见）
     */
    @GetMapping("/current")
    public Result<List<AnnouncementVO>> getCurrentAnnouncements() {
        List<Announcement> announcements = announcementService.getCurrentAnnouncements();
        List<AnnouncementVO> voList = announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取所有公告（管理员）
     */
    @GetMapping("/list")
    public Result<List<AnnouncementVO>> getAllAnnouncements(HttpSession session) {
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole == 0) {
            return Result.error("权限不足");
        }

        List<Announcement> announcements = announcementService.getAllAnnouncements();
        List<AnnouncementVO> voList = announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 分页获取公告（管理员）
     */
    @GetMapping("/page")
    public Result<PageResult<AnnouncementVO>> getAnnouncementsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session) {

        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole == 0) {
            return Result.error("权限不足");
        }

        List<Announcement> announcements = announcementService.getAnnouncementsByPage(pageNum, pageSize);
        int total = announcementService.getTotalCount();

        List<AnnouncementVO> voList = announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<AnnouncementVO> pageResult = new PageResult<>(pageNum, pageSize, total, voList);
        return Result.success(pageResult);
    }

    /**
     * 根据类型获取公告
     */
    @GetMapping("/type/{type}")
    public Result<List<AnnouncementVO>> getAnnouncementsByType(@PathVariable Integer type) {
        List<Announcement> announcements = announcementService.getAnnouncementsByType(type);
        List<AnnouncementVO> voList = announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(convertToVO(announcement));
    }

    /**
     * 发布公告（管理员）
     */
    @PutMapping("/add")
    public Result<Void> addAnnouncement(@RequestBody Announcement announcement, HttpSession session) {
        Integer userRole = (Integer) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");

        if (userRole == null || userRole != 1 && userRole != 2) {
            return Result.error("只有管理员可以发布公告");
        }

        announcement.setPublisherId(userId);

        try {
            announcementService.addAnnouncement(announcement);
            return Result.success();
        } catch (Exception e) {
            log.error("发布公告失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/update")
    public Result<Void> updateAnnouncement(@RequestBody Announcement announcement, HttpSession session) {
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != 1 && userRole != 2) {
            return Result.error("只有管理员可以更新公告");
        }

        try {
            announcementService.updateAnnouncement(announcement);
            return Result.success();
        } catch (Exception e) {
            log.error("更新公告失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新公告状态（管理员）
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateAnnouncementStatus(@PathVariable Long id,
                                                 @RequestParam Integer status,
                                                 HttpSession session) {
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != 1 && userRole != 2) {
            return Result.error("只有管理员可以更新公告状态");
        }

        try {
            announcementService.updateAnnouncementStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新公告状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id, HttpSession session) {
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != 1 && userRole != 2) {
            return Result.error("只有管理员可以删除公告");
        }

        try {
            announcementService.deleteAnnouncement(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 转换方法：Announcement -> AnnouncementVO
     */
    private AnnouncementVO convertToVO(Announcement announcement) {
        if (announcement == null) return null;

        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);

        // 公告类型转换
        if (announcement.getType() != null) {
            switch (announcement.getType()) {
                case 1:
                    vo.setTypeText("系统公告");
                    vo.setTypeColor("primary");
                    break;
                case 2:
                    vo.setTypeText("场馆通知");
                    vo.setTypeColor("success");
                    break;
                case 3:
                    vo.setTypeText("活动通知");
                    vo.setTypeColor("warning");
                    break;
                default:
                    vo.setTypeText("其他");
                    vo.setTypeColor("info");
            }
        }

        // 优先级转换
        if (announcement.getPriority() != null) {
            switch (announcement.getPriority()) {
                case 0:
                    vo.setPriorityText("普通");
                    vo.setPriorityColor("info");
                    break;
                case 1:
                    vo.setPriorityText("重要");
                    vo.setPriorityColor("warning");
                    break;
                case 2:
                    vo.setPriorityText("紧急");
                    vo.setPriorityColor("danger");
                    break;
                default:
                    vo.setPriorityText("普通");
                    vo.setPriorityColor("info");
            }
        }

        // 状态转换
        if (announcement.getStatus() != null) {
            vo.setStatusText(announcement.getStatus() == 1 ? "已发布" : "已下架");
        }

        // 发布人信息
        if (announcement.getPublisher() != null) {
            vo.setPublisherName(announcement.getPublisher().getName());
        }

        return vo;
    }
}