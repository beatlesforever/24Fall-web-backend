package com.example.backend.controller;

import com.example.backend.entity.Order;
import com.example.backend.service.IOrderService;
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
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    IOrderService orderService;

    /**
     * 创建订单。
     *
     * @param order 包含订单信息的对象，通过请求体传入。
     * @param authentication 当前用户的认证信息，用于权限验证。
     * @return 如果认证失败，返回未授权的状态码；否则，返回创建成功的订单信息。
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order, Authentication authentication) {
        // 验证用户是否已认证，未认证则返回401状态码
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 保存订单
        orderService.save(order);
        return ResponseEntity.ok(order);
    }

    /**
     * 根据订单ID获取订单信息。
     *
     * @param orderId 通过路径变量传递的订单ID。
     * @param authentication 当前请求的认证信息，用于权限验证。
     * @return 如果找到相应的订单，返回包含订单信息的ResponseEntity；如果没有找到，返回一个订单未找到的ResponseEntity；如果用户未认证，返回未授权的ResponseEntity。
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Integer orderId, Authentication authentication) {
        // 验证用户是否已认证，未认证返回401状态码
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 根据订单ID获取订单
        Order order = orderService.getById(orderId);
        // 订单存在返回200状态码，否则返回404状态码
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }


    /**
     * 获取指定用户的所有订单。
     *
     * @param userId 用户ID，通过URL路径变量传递。表示需要查询订单的用户ID。
     * @param authentication 当前请求的认证信息，用于权限验证。
     * @return 返回一个包含该用户所有订单的响应实体。如果用户没有订单，则返回空列表。
     *         如果用户未进行认证，返回401未授权状态。
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Integer userId, Authentication authentication) {
        // 验证用户是否已认证，未认证返回401状态码
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 根据用户ID查询其所有订单
        List<Order> orders = orderService.lambdaQuery().eq(Order::getUserId, userId).list();
        return ResponseEntity.ok(orders);
    }


    /**
     * 更新订单状态
     *
     * @param orderId 订单ID，通过URL路径变量传递，用于确定需要更新状态的具体订单
     * @param status 订单的新状态，通过请求体（JSON字符串）传递，表示订单更新后的状态
     * @param authentication 当前请求的认证信息，用于验证请求者的身份
     * @return 如果订单状态更新成功，则返回200 OK的响应实体，表示操作成功；如果找不到对应的订单，则返回404 Not Found的响应实体，表示操作失败。
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer orderId, @RequestBody String status, Authentication authentication) {
        // 验证用户是否已通过认证，若未认证，则返回401 Unauthorized
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 使用lambda表达式尝试更新订单状态，根据订单ID设置新的状态
        boolean updated = orderService.lambdaUpdate().eq(Order::getOrderId, orderId).set(Order::getStatus, status).update();
        // 根据更新操作的结果，返回相应的HTTP响应实体
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


}
