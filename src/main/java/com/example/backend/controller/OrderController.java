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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }
    /**
     * 创建订单。
     *
     * @param order 包含订单信息的对象，通过请求体传入。订单信息包括用户ID、商品信息、总价格等。
     * @param authentication 当前用户的认证信息，用于权限验证。确保只有已认证的用户才能创建订单。
     * @return 如果认证失败，返回未授权的状态码（401）；否则，返回创建成功的订单信息，包括订单ID、用户ID、状态、总价格、下单时间等。
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order, Authentication authentication) {
        // 权限验证：判断用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 初始化订单状态和时间
        order.setTotalPrice(BigDecimal.ZERO);  // 初始金额设置为0
        order.setStatus(OrderStatus.PENDING.toString());  // 订单状态设置为待处理
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));  // 设置订单时间为当前时间

        // 保存订单到数据库
        orderService.save(order);

        // 准备订单创建成功后返回的数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("userId", order.getUserId());
        data.put("status", order.getStatus());
        data.put("totalPrice", order.getTotalPrice());
        data.put("orderTime", order.getOrderTime().toString());
        data.put("notes", order.getNotes());

        // 返回订单创建成功的响应，包含订单详细信息
        return createResponse(HttpStatus.OK, "订单创建成功", data);
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
        // 检查用户是否认证
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        // 根据订单ID获取订单
        Order order = orderService.getById(orderId);
        // 如果订单不存在，返回未找到的响应
        if (order == null) {
            return createResponse(HttpStatus.NOT_FOUND, "订单未找到", null);
        }

        // 构建订单信息的响应体
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("userId", order.getUserId());
        data.put("status", order.getStatus());
        data.put("totalPrice", order.getTotalPrice());
        data.put("orderTime", order.getOrderTime().toString());
        data.put("notes", order.getNotes());

        // 返回订单信息的响应
        return createResponse(HttpStatus.OK, "订单信息获取成功", data);
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
        // 验证用户是否认证
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        // 根据用户ID查询订单
        List<Order> orders = orderService.lambdaQuery().eq(Order::getUserId, userId).list();

        // 将订单信息转换为简洁的Map格式
        List<Map<String, Object>> orderDetails = orders.stream().map(order -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("orderId", order.getOrderId());
            detail.put("userId", order.getUserId());
            detail.put("status", order.getStatus());
            detail.put("totalPrice", order.getTotalPrice());
            detail.put("orderTime", order.getOrderTime().toString());
            detail.put("notes", order.getNotes());
            return detail;
        }).collect(Collectors.toList());

        // 准备返回的数据结构
        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderDetails);
        // 构造成功返回的响应实体
        return createResponse(HttpStatus.OK, "用户订单列表获取成功", data);
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

    /**
     * 获取所有订单的信息列表。
     * <p>
     * 该接口不接受任何参数，返回所有订单的详细信息列表，包括订单号、用户ID、状态、总价、下单时间和备注。
     * <p>
     * 返回值: ResponseEntity<?> 包含订单信息的HTTP响应实体，包括状态码、消息和数据部分。
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        // 从订单服务获取所有订单列表
        List<Order> orders = orderService.list();

        // 将订单列表映射转换为订单详情列表，每个详情为一个Map，包含订单的各种属性
        List<Map<String, Object>> orderDetails = orders.stream().map(order -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("orderId", order.getOrderId());
            detail.put("userId", order.getUserId());
            detail.put("status", order.getStatus());
            detail.put("totalPrice", order.getTotalPrice());
            detail.put("orderTime", order.getOrderTime().toString());
            detail.put("notes", order.getNotes());
            return detail;
        }).collect(Collectors.toList());

        // 将订单详情列表封装到一个大Map中，作为数据部分返回
        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderDetails);

        // 创建并返回一个包含状态码、消息和数据的响应实体
        return createResponse(HttpStatus.OK, "所有订单列表获取成功", data);
    }




}
