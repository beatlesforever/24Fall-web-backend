package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */
@Data
@TableName("reviews")
public class Review {
    @TableId
    private Integer reviewId;
    private Integer userId;
    private Integer itemId;
    private Integer rating;
    private String comment;
    private Timestamp reviewTime;
}
