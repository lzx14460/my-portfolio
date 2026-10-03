package com.example.system.service.impl;

import com.example.system.entity.Announcement;
import com.example.system.mapper.AnnouncementMapper;
import com.example.system.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 根据ID查询公告
     */
    @Override
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    /**
     * 获取所有公告列表
     */
    @Override
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.selectAll();
    }

    /**
     * 分页查询公告列表
     */
    @Override
    public List<Announcement> getAnnouncementsByPage(int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        return announcementMapper.selectByPage(start, pageSize);
    }

    /**
     * 获取公告总数
     */
    @Override
    public int getTotalCount() {
        return announcementMapper.selectCount();
    }

    /**
     * 根据类型查询公告
     */
    @Override
    public List<Announcement> getAnnouncementsByType(Integer type) {
        return announcementMapper.selectByType(type);
    }

    /**
     * 获取当前有效公告（已发布且未过期）
     */
    @Override
    public List<Announcement> getCurrentAnnouncements() {
        return announcementMapper.selectCurrentAnnouncements();
    }

//    @Override
//    public List<Announcement> getAnnouncementsByPublisherId(Long publisherId) {
//        return announcementMapper.selectByPublisherId(publisherId);
//    }

    /**
     * 发布公告（含默认值设置）
     */
    @Override
    @Transactional
    public int addAnnouncement(Announcement announcement) {
        // 设置默认值
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(new Date());
        }

        int result = announcementMapper.insert(announcement);
        log.info("发布公告成功: title={}", announcement.getTitle());
        return result;
    }

    /**
     * 更新公告内容
     */
    @Override
    @Transactional
    public int updateAnnouncement(Announcement announcement) {
        int result = announcementMapper.update(announcement);
        log.info("更新公告成功: id={}", announcement.getId());
        return result;
    }

    /**
     * 更新公告发布/下架状态
     */
    @Override
    @Transactional
    public int updateAnnouncementStatus(Long id, Integer status) {
        int result = announcementMapper.updateStatus(id, status);
        log.info("更新公告状态: id={}, status={}", id, status);
        return result;
    }

    /**
     * 删除公告
     */
    @Override
    @Transactional
    public int deleteAnnouncement(Long id) {
        int result = announcementMapper.deleteById(id);
        log.info("删除公告: id={}", id);
        return result;
    }
}