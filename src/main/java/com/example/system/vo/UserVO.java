package com.example.system.vo;

import lombok.Data;
import java.util.Date;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String name;
    private String genderText;      // 性别文本
    private String phone;
    private String email;
    private Integer role;
    private String roleText;        // 角色文本
    private String avatar;
    private String studentId;
    private String college;
    private String className;
    private Integer status;
    private String statusText;      // 状态文本
    private Date createTime;
    private Integer appliedRole;
    private Integer applyStatus;
    private Date applyTime;
    private Date approveTime;
}