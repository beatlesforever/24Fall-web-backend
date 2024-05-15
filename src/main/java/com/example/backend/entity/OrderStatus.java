package com.example.backend.entity;

/**
 * @author zhouhaoran
 * @date 2024/4/16
 * @project Backend
 */
public enum OrderStatus {
    CREATED("已创建"),       // 订单已创建
    IN_PROGRESS("进行中"),   // 订单正在进行中
    COMPLETED("已完成"),     // 订单已完成
    CANCELLED("已取消"),     // 订单已取消
    REFUNDED("已退款");      // 订单已退款

    private final String status;  // 中文描述的订单状态

    /**
     * 枚举构造函数，用于初始化订单状态的中文描述。
     *
     * @param status 订单状态的中文描述
     */
    OrderStatus(String status) {
        this.status = status;
    }

    /**
     * 返回订单状态的中文描述。
     *
     * @return 订单状态的中文描述
     */
    @Override
    public String toString() {
        return this.status;
    }

    /**
     * 从字符串解析为对应的订单状态枚举实例。
     *
     * @param status 订单状态的中文描述
     * @return 对应的订单状态枚举实例
     * @throws IllegalArgumentException 如果给定的字符串不匹配任何已定义的订单状态
     */
    public static OrderStatus fromString(String status) {
        for (OrderStatus os : OrderStatus.values()) {
            if (os.status.equalsIgnoreCase(status)) {
                return os;
            }
        }
        throw new IllegalArgumentException("未知状态: " + status);
    }
}
