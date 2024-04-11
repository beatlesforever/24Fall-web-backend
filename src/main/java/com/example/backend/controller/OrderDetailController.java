package com.example.backend.controller;

import com.example.backend.entity.OrderDetail;
import com.example.backend.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@RestController
@RequestMapping("/api/orderDetails")
public class OrderDetailController {
    @Autowired
    IOrderDetailService orderDetailService;

    /**
     * 根据订单ID获取订单详情列表。
     *
     * @param orderId 通过路径变量传递的订单ID，用于查询对应的订单详情。
     * @return 返回一个响应实体，包含指定订单ID的所有订单详情列表。
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetail>> getOrderDetails(@PathVariable Integer orderId) {
        // 使用lambda查询方式，根据订单ID查询订单详情列表
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();
        return ResponseEntity.ok(orderDetails);
    }

    /**
     * 添加订单详情信息。
     *
     * @param orderDetail 订单详情对象，通过RequestBody接收前端传来的订单详情数据。
     * @param authentication 用户认证信息，用于判断用户是否已认证。
     * @return 返回响应实体，如果用户未认证，返回401状态码；否则，保存订单详情后返回200状态码和订单详情对象。
     */
    @PostMapping
    public ResponseEntity<?> addOrderDetail(@RequestBody OrderDetail orderDetail, Authentication authentication) {
        // 检查用户认证状态，如果未认证，返回401状态码
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 保存订单详情
        orderDetailService.save(orderDetail);
        // 返回成功响应，包含订单详情对象
        return ResponseEntity.ok(orderDetail);
    }
}
