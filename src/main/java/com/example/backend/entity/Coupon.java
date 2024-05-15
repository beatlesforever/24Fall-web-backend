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

    /**
     * 优惠券ID，主键，自增。
     */
    @TableId(type = IdType.AUTO)
    private Integer couponId;

    /**
     * 优惠券代码，用户在购物时输入以获得折扣。
     * 例如：DISCOUNT2024
     */
    private String code;

    /**
     * 折扣金额，表示优惠券可以抵扣的金额。
     * 使用BigDecimal以确保精确度。
     */
    private BigDecimal discount;

    /**
     * 优惠券的到期日期，表示该优惠券的有效期。
     * 超过该日期后优惠券将不可使用。
     */
    private Date expirationDate;

    /**
     * 最低消费金额，表示使用该优惠券所需的最低订单金额。
     * 使用BigDecimal以确保精确度。
     */
    private BigDecimal minPurchase;

    /**
     * 优惠券是否处于激活状态。
     * true表示优惠券可用，false表示优惠券不可用。
     */
    private Boolean isActive;
}

