package com.example.backend.controller;

import com.example.backend.entity.Order;
import com.example.backend.entity.OrderDetail;
import com.example.backend.service.IOrderDetailService;
import com.example.backend.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    @Autowired
    IOrderService orderService; // 注入订单服务
    /**
     * 根据订单ID获取订单详情列表。
     *
     * @param orderId 通过路径变量传递的订单ID，用于查询对应的订单详情。
     * @return 返回一个响应实体，包含指定订单ID的所有订单详情列表。
     */
    @GetMapping("/{orderId}")
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

        // 更新订单的总金额
        updateOrderTotalPrice(orderDetail.getOrderId());

        // 返回成功响应，包含订单详情对象
        return ResponseEntity.ok(orderDetail);
    }

    /**
     * 更新指定订单的总价格。
     *
     * @param orderId 订单ID，用于标识需要更新总价格的订单。
     *                该方法会根据提供的订单ID，查询该订单的所有订单详情，
     *                计算出新的总价格，并更新到订单信息中。
     */
    private void updateOrderTotalPrice(Integer orderId) {
        // 使用订单ID查询所有相关的订单详情项
        // lambdaQuery() 创建一个查询构造器，eq() 指定查询条件，即订单ID等于传入的orderId
        List<OrderDetail> details = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();

        // 使用Java 8的流（Stream）来计算订单的总价格
        // map() 方法用于转换每个订单详情项的价格，通过价格乘以数量来计算每个详情项的总价
        // reduce() 方法用于将所有订单详情项的总价累加起来，如果没有详情项，初始值为BigDecimal.ZERO
        BigDecimal newTotalPrice = details.stream()
                .map(detail -> detail.getPrice().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取对应的订单实体
        Order order = orderService.getById(orderId);
        if (order != null) { // 确保查询到的订单实体存在
            order.setTotalPrice(newTotalPrice); // 设置订单的新的总价格
            orderService.updateById(order); // 调用服务层的方法更新订单实体，将新的总价格保存到数据库中
        }
    }

}
