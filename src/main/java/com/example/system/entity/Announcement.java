package com.example.system.entity;

import lombok.Data;
import java.util.Date;

/**
 * 公告实体类
 */
@Data
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private Integer type;        // 1-系统公告，2-场馆通知，3-活动通知
    private Integer priority;    // 0-普通，1-重要，2-紧急
    private Long publisherId;
    private Integer status;      // 0-下架，1-发布
    private Date publishTime;
    private Date expireTime;
    private Date createTime;

    // 关联字段
    private User publisher;
}