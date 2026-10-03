package com.example.system.mapper;

import com.example.system.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AnnouncementMapper {

    Announcement selectById(Long id);

    List<Announcement> selectAll();

    List<Announcement> selectByPage(@Param("start") int start, @Param("size") int size);

    int selectCount();

    List<Announcement> selectByType(Integer type);

    List<Announcement> selectCurrentAnnouncements();

//    List<Announcement> selectByPublisherId(Long publisherId);

    int insert(Announcement announcement);

    int update(Announcement announcement);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);
}