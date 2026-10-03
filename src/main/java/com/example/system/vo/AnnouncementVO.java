package com.example.system.vo;

import lombok.Data;
import java.util.Date;

/**
 * 公告视图对象
 */
@Data
public class AnnouncementVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private String typeText;         // 公告类型文本
    private String typeColor;        // 类型标签颜色
    private Integer priority;
    private String priorityText;     // 优先级文本
    private String priorityColor;    // 优先级标签颜色
    private Long publisherId;
    private String publisherName;    // 发布人姓名
    private Integer status;
    private String statusText;       // 状态文本
    private Date publishTime;
    private Date expireTime;
}