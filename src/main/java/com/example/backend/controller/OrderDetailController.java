package com.example.backend.controller;

import com.example.backend.entity.MenuItem;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderDetail;
import com.example.backend.service.IMenuItemService;
import com.example.backend.service.IOrderDetailService;
import com.example.backend.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    IMenuItemService menuItemService;
    /**
     * 创建统一格式的响应。
     *
     * @param status HTTP状态码
     * @param message 响应消息
     * @param data 响应包含的数据
     * @return ResponseEntity包含状态码、消息和数据
     */
    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 根据订单ID获取订单详情列表。
     *
     * 此方法通过接收指定的订单ID，从服务层获取相应的订单详情列表，并封装成响应实体返回。
     * 主要用于前端接口调用，以获取特定订单的所有详情信息。
     *
     * @param orderId 通过路径变量传递的订单ID，用于查询对应的订单详情。
     *                该参数由URL路径直接传递，保证了请求的订单ID的准确性。
     * @return 返回一个响应实体，包含指定订单ID的所有订单详情列表。
     *         响应实体中除了订单详情列表外，还可能包含其他状态信息，如操作状态和消息等。
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Integer orderId) {
        // 首先检查订单是否存在
        if (orderService.getById(orderId) == null) {
            // 如果订单不存在，返回错误信息
            return createResponse(HttpStatus.NOT_FOUND, "订单不存在", null);
        }
        // 通过订单ID查询订单详情列表
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();

        // 将查询结果封装到Map中，方便构建响应实体
        Map<String, Object> data = new HashMap<>();
        data.put("orderDetails", orderDetails);

        // 创建并返回响应实体，包含订单详情数据和状态信息
        return createResponse(HttpStatus.OK, "成功获取数据", data);
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
        // 检查用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }

        // 检查订单是否存在
        Order order = orderService.getById(orderDetail.getOrderId());
        if (order == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "订单不存在", null);
        }

        // 检查菜品是否存在
        MenuItem item = menuItemService.getById(orderDetail.getItemId());
        if (item == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "菜品不存在", null);
        }

        // 设置订单详情中的价格为菜品价格
        orderDetail.setPrice(item.getPrice());

        // 保存订单详情信息
        orderDetailService.save(orderDetail);
        // 更新订单总价格
        updateOrderTotalPrice(orderDetail.getOrderId());
        // 准备响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderDetailId", orderDetail.getOrderId());
        data.put("orderId", orderDetail.getOrderId());
        data.put("itemId", orderDetail.getItemId());
        data.put("quantity", orderDetail.getQuantity());
        data.put("price", orderDetail.getPrice());
        data.put("size", orderDetail.getSize());
        // 创建并返回响应实体
        return createResponse(HttpStatus.OK, "订单详情添加成功", data);
    }


    /**
     * 删除指定订单的某个订单详情。
     *
     * @param orderId 订单ID，通过路径变量传递。
     * @param orderDetailId 订单详情ID，通过路径变量传递。
     * @param authentication 用户认证信息。
     * @return 返回操作的结果，包括成功或错误信息。
     */
    @DeleteMapping("/{orderId}/{orderDetailId}")
    public ResponseEntity<?> deleteOrderDetail(@PathVariable Integer orderId, @PathVariable Integer orderDetailId, Authentication authentication) {
        // 验证用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 验证订单是否存在
        Order order = orderService.getById(orderId);
        if (order == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "订单不存在", null);
        }

        // 验证订单详情是否存在
        OrderDetail orderDetail = orderDetailService.getById(orderDetailId);
        if (orderDetail == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "订单详情不存在", null);
        }

        // 确认订单详情属于正确的订单
        if (!orderDetail.getOrderId().equals(orderId)) {
            return createResponse(HttpStatus.BAD_REQUEST, "订单详情不属于该订单", null);
        }

        // 删除订单详情
        orderDetailService.removeById(orderDetailId);

        // 更新订单总价格
        updateOrderTotalPrice(orderId);

        // 返回成功响应
        return createResponse(HttpStatus.OK, "订单详情删除成功", null);
    }


    /**
     * 更新指定订单的总价格。
     *
     * @param orderId 订单ID，用于标识需要更新总价格的订单。
     *
     * 本方法通过查询指定订单的所有订单详情，计算新总价格，并更新订单的总价格。
     */
    private void updateOrderTotalPrice(Integer orderId) {
        // 根据订单ID查询订单详情列表
        List<OrderDetail> details = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();

        // 计算订单详情的总价格
        BigDecimal newTotalPrice = details.stream()
                .map(detail -> detail.getPrice().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取订单对象
        Order order = orderService.getById(orderId);
        if (order != null) {
            // 如果订单存在，则更新订单的总价格
            order.setTotalPrice(newTotalPrice);
            orderService.updateById(order);
        }
    }




}
