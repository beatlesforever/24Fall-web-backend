package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("users")
public class User {
    @TableId
    private Integer userId;
    private String name;
    private String phone;
    private String email;
    private String password;
    private Date registrationDate;
    private BigDecimal balance;
}
