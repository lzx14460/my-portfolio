package com.example.system.service;

import com.example.system.entity.Announcement;
import java.util.List;

public interface AnnouncementService {

    Announcement getAnnouncementById(Long id);

    List<Announcement> getAllAnnouncements();

    List<Announcement> getAnnouncementsByPage(int pageNum, int pageSize);

    int getTotalCount();

    List<Announcement> getAnnouncementsByType(Integer type);

    List<Announcement> getCurrentAnnouncements();

//    List<Announcement> getAnnouncementsByPublisherId(Long publisherId);

    int addAnnouncement(Announcement announcement);

    int updateAnnouncement(Announcement announcement);

    int updateAnnouncementStatus(Long id, Integer status);

    int deleteAnnouncement(Long id);
}