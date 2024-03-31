package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("users")
public class User{
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String name;
    private String phone;
    @JsonIgnore // 这会阻止密码被序列化到JSON
    private String password;
    private Date registrationDate;
    private BigDecimal balance;
}
