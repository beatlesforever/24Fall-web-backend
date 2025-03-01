package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("menu_items")
public class MenuItem {
    @TableId(type = IdType.AUTO)
    private Integer itemId;
    private Integer storeId;
    private String name;
    private String description;
    private String imageUrl;
    private String category;
    private BigDecimal smallSizePrice;
    private BigDecimal largeSizePrice;
    private Integer sizeStock;
}
