package com.example.backend.entity;

/**
 * @author zhouhaoran
 * @date 2024/4/16
 * @project Backend
 */
public enum OrderStatus {
    PENDING("已下单"),
    COMPLETED("已完成");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.status;
    }

    public static OrderStatus fromString(String status) {
        for (OrderStatus os : OrderStatus.values()) {
            if (os.status.equalsIgnoreCase(status)) {
                return os;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}
