package com.example.backend.dto;

import lombok.Data;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@Data
public class UserRegisterDTO {
    private String name;
    private String phone;
    private String password;
    private String role; // 新增角色字段

}
