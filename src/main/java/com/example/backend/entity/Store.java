package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zhouhaoran
 * @date 2024/5/14
 * @project Backend
 */
@Data
@TableName("stores")
public class Store {
    @TableId(type = IdType.AUTO)
    private Integer storeId;
    private String name;
    private String location;
}

