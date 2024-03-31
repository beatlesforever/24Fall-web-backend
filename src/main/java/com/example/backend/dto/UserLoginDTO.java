package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author zhouhaoran
 * @date 2024/3/30
 * @project Backend
 */
@Data
public class UserLoginDTO {
    private String phone;
    private String password;
}
