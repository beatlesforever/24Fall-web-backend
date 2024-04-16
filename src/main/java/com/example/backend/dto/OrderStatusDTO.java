package com.example.backend.dto;

import lombok.Data;

/**
 * @author zhouhaoran
 * @date 2024/4/16
 * @project Backend
 */
@Data
public class OrderStatusDTO {
    private String status; // 保持为字符串类型用来接收前端传来的状态值

    public OrderStatusDTO() {
    }

    public OrderStatusDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
