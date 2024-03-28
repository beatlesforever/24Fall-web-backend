package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("orders")
public class Order {
    @TableId
    private Integer orderId;
    private Integer userId;
    private String status;
    private BigDecimal totalPrice;
    private Timestamp orderTime;
    private String notes;
}
