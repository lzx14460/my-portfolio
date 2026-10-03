package com.example.system.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String name;
    private Integer gender;      // 0-女，1-男
    private String phone;
    private String email;
    private Integer role;        // 0-学生，1-场馆管理员，2-系统管理员
    private String avatar;
    private String studentId;
    private String college;
    private String className;
    private Integer status;      // 0-禁用，1-启用
    private Date createTime;
    private Date updateTime;
    private Integer appliedRole;      // 申请的角色：1-场馆管理员，2-系统管理员
    private Integer applyStatus;      // 申请状态：0-未申请，1-待审批，2-已通过，3-已拒绝
    private Date applyTime;           // 申请时间
    private Date approveTime;         // 审批时间
    private String approveRemark;     // 审批备注
}