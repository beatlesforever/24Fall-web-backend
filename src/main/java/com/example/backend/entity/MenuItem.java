package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("menu_items")
public class MenuItem {
    @TableId
    private Integer itemId;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private String category;
}
