package com.example.backend.controller;

import com.example.backend.dto.OrderStatusDTO;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderStatus;
import com.example.backend.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
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

        // 设置订单的默认值
        order.setTotalPrice(BigDecimal.ZERO);   // 设置默认金额为0
        order.setStatus(OrderStatus.PENDING.toString()); // 设置默认状态为“已下单”，并使用枚举的中文描述
        order.setOrderTime(new Timestamp(System.currentTimeMillis())); // 设置当前时间为订单时间

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
     * @param statusUpdate 包含订单状态更新信息的对象，通过请求体传递
     * @param authentication 当前请求的认证信息，用于验证请求者的身份
     * @return 如果订单状态更新成功，则返回200 OK的响应实体，表示操作成功；如果找不到对应的订单，则返回404 Not Found的响应实体，表示操作失败。
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer orderId, @RequestBody OrderStatusDTO statusUpdate, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }

        try {
            OrderStatus newStatus = OrderStatus.fromString(statusUpdate.getStatus()); // 将字符串转换为枚举
            boolean updated = orderService.lambdaUpdate()
                    .eq(Order::getOrderId, orderId)
                    .set(Order::getStatus, newStatus.toString()) // 存储枚举对应的中文描述
                    .update();

            return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的订单状态");
        }
    }





}
