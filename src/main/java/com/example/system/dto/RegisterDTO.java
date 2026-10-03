package com.example.system.dto;


import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String name;
    private String email;
    private String phone;
    private String studentId;      // 学号
    private String college;        // 学院
    private String className;      // 班级

    @NotNull(message = "请选择注册角色")
    private Integer role;  // 0-学生，1-场馆管理员，2-系统管理员
}