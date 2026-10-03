package com.example.system.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UserUpdateDTO {
    private Long id;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String studentId;
    private String college;
    private String className;

    // 注意：不包含 phone 和 email
}