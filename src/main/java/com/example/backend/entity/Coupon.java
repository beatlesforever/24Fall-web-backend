package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhouhaoran
 * @date 2024/5/14
 * @project Backend
 */
@Data
@TableName("coupons")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Integer couponId;
    private String code;
    private BigDecimal discount;
    private Date expirationDate;
    private BigDecimal minPurchase;
    private Boolean isActive;
}

