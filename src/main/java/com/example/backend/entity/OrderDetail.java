package com.example.backend.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_details")
public class OrderDetail {
    @TableId(type = IdType.AUTO)
    private Integer detailId;
    private Integer orderId;
    private Integer itemId;
    private Integer quantity;
    private BigDecimal price;
    private String size;
}