package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.backend.entity.*;
import com.example.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
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
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    IReviewService reviewService;
    @Autowired
    IMenuItemService menuItemService;
    @Autowired
    IUserService userService;

    @Autowired
    IOrderDetailService orderDetailService;

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
     * 添加评价
     *
     * @param review 用户提交的评价信息，通过RequestBody接收。包含评价的具体内容等。
     * @param authentication 当前用户的认证信息，用于判断用户是否认证。由Spring Security提供。
     * @return 根据操作结果返回不同的响应实体。成功则返回200 OK和评价添加成功的消息，未认证则返回401 Unauthorized和相应消息。
     */
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review, Authentication authentication) {
        // 检查用户是否认证，未认证则返回401状态码和相应消息
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }

        // 检查评分是否在合理范围内
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return createResponse(HttpStatus.BAD_REQUEST, "评分必须在1到5之间", null);
        }

        // 设置当前服务器时间为评价时间，确保评价时间的准确性
        review.setReviewTime(Timestamp.from(Instant.now()));

        // 保存评价到数据库
        reviewService.save(review);

        // 构建返回给客户端的响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("review", review);

        // 创建并返回响应实体
        return createResponse(HttpStatus.OK, "评价添加成功", data);

    }



    /**
     * 修改评价的API接口
     *
     * @param reviewId 评价的ID，用于指定要修改的评价
     * @param review 包含修改后评价信息的对象
     * @param authentication 当前用户的认证信息，用于权限验证
     * @return 根据操作结果返回不同的HTTP状态码和消息：
     *         如果用户未认证，返回401状态码和"未认证的用户"消息；
     *         如果评价更新成功，返回200状态码和"评价更新成功"消息；
     *         如果评价未找到，返回404状态码。
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Integer reviewId, @RequestBody Review review, Authentication authentication) {
        // 验证用户是否认证
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 检查评分是否在合理范围内
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return createResponse(HttpStatus.BAD_REQUEST, "评分必须在1到5之间", null);
        }

        // 设置评价ID
        review.setReviewId(reviewId);

        // 更新评价信息
        boolean updated = reviewService.updateById(review);

        // 更新成功后，返回更新后的评价信息
        if (updated) {
            Review updatedReview = reviewService.getById(reviewId);

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("review", updatedReview);
            return createResponse(HttpStatus.OK, "评价更新成功", reviewData);
        } else {
            // 评价未找到时，返回错误信息
            return createResponse(HttpStatus.NOT_FOUND, "评价未找到", null);
        }
    }



    /**
     * 删除评价
     *
     * @param reviewId 评价的ID，用于指定要删除的评价
     * @param authentication 当前用户的认证信息，用于检查用户是否已认证
     * @return 返回一个响应实体，如果删除成功，则返回200 OK和删除成功的消息；如果删除失败（如评价不存在），则返回404 Not Found；如果用户未认证，则返回401 Unauthorized。
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 删除评价
        boolean removed = reviewService.removeById(reviewId);

        if (removed) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reviewId", reviewId);
            return createResponse(HttpStatus.OK, "评价删除成功", responseData);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "评价未找到", null);
        }
    }


    /**
     * 根据菜品ID获取其所有评价
     *
     * @param itemId 菜品的ID，作为查询条件
     * @return 返回一个响应实体，包含指定菜品ID的所有评价列表
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<Map<String, Object>> getReviewsByItemId(@PathVariable Integer itemId) {

        // 查询菜品是否存在
        boolean itemExists = menuItemService.getById(itemId) != null;

        // 如果菜品不存在，返回404 Not Found
        if (!itemExists) {
            return createResponse(HttpStatus.NOT_FOUND, "菜品ID不存在", null);
        }

        // 获取指定菜品ID的所有评价
        List<Review> reviews = reviewService.lambdaQuery().eq(Review::getItemId, itemId).list();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("reviews", reviews);
        return createResponse(HttpStatus.OK, "查询成功", responseData);
    }


    /**
     * 根据用户ID获取该用户的所有评价
     *
     * @param userId 用户的ID，作为查询条件
     * @return 返回一个响应实体，包含该用户的所有评价列表。
     *         如果查询成功，响应状态码为200 OK，返回的响应体中包含"reviews"字段，其值为用户评价列表。
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReviewsByUserId(@PathVariable Integer userId) {
        // 查询用户是否存在
        boolean userExists = userService.getById(userId) != null;

        // 如果用户不存在，返回404 Not Found
        if (!userExists) {
            return createResponse(HttpStatus.NOT_FOUND, "用户ID不存在", null);
        }

        // 根据用户ID查询该用户的所有评价
        List<Review> reviews = reviewService.lambdaQuery().eq(Review::getUserId, userId).list();


        // 准备响应数据，包含评价列表
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("reviews", reviews);

        // 构建并返回响应实体，状态码为200 OK，附带查询成功的消息和查询结果
        return createResponse(HttpStatus.OK, "查询成功", responseData);
    }


    /**
     * 获取指定菜品ID的统计信息，包括基本信息、平均评分、评价数量、销量以及所有评论信息内容。
     *
     * @param itemId 菜品的ID，用于查询对应的统计数据
     * @return ResponseEntity<Map<String, Object>> 返回一个包含统计信息的响应实体，
     *         如果菜品ID不存在，则返回状态码为404的响应
     */
    @GetMapping("/item/{itemId}/statistics")
    public ResponseEntity<Map<String, Object>> getItemStatistics(@PathVariable Integer itemId) {
        // 检查菜品是否存在
        MenuItem menuItem = menuItemService.getById(itemId);
        if (menuItem == null) {
            return createResponse(HttpStatus.NOT_FOUND, "菜品ID不存在", null);
        }

        // 获取所有已完成的订单，用于后续计算销量
        List<Order> completedOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatus.COMPLETED.toString())
                .list();

        // 获取所有订单详情，用于统计该菜品的销量
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery()
                .eq(OrderDetail::getItemId, itemId)
                .list();

        Map<String, Object> stats = new HashMap<>();
        // 菜品基本信息
        stats.put("itemId", menuItem.getItemId());
        stats.put("name", menuItem.getName());
        stats.put("description", menuItem.getDescription());
        stats.put("imageUrl", menuItem.getImageUrl());
        stats.put("category", menuItem.getCategory());
        stats.put("smallSizePrice", menuItem.getSmallSizePrice());
        stats.put("largeSizePrice", menuItem.getLargeSizePrice());
        stats.put("sizeStock", menuItem.getSizeStock());

        // 计算平均评分
        QueryWrapper<Review> avgWrapper = Wrappers.query();
        avgWrapper.select("AVG(rating) as avgRating")
                .eq("item_id", itemId);
        Map<String, Object> avgResult = reviewService.getMap(avgWrapper);
        BigDecimal avgRatingDecimal = avgResult != null ? (BigDecimal) avgResult.get("avgRating") : BigDecimal.ZERO;
        Double averageRating = avgRatingDecimal.doubleValue();
        stats.put("averageRating", averageRating);

        // 统计该菜品的评价数量
        long totalReviews = reviewService.lambdaQuery()
                .eq(Review::getItemId, itemId)
                .count();
        stats.put("totalReviews", totalReviews);

        // 获取该菜品的所有评价
        List<Review> reviews = reviewService.lambdaQuery()
                .eq(Review::getItemId, itemId)
                .list();
        List<Map<String, Object>> reviewDetails = reviews.stream().map(review -> {
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("reviewId", review.getReviewId());
            reviewData.put("userId", review.getUserId());
            reviewData.put("itemId", review.getItemId());
            reviewData.put("rating", review.getRating());
            reviewData.put("comment", review.getComment());
            reviewData.put("reviewTime", review.getReviewTime());
            return reviewData;
        }).collect(Collectors.toList());
        stats.put("reviews", reviewDetails);

        // 统计销量，筛选出对应菜品且订单已完成的订单详情，然后累加销量
        long totalSales = orderDetails.stream()
                .filter(orderDetail -> completedOrders.stream()
                        .anyMatch(order -> order.getOrderId().equals(orderDetail.getOrderId())))
                .mapToLong(OrderDetail::getQuantity)
                .sum();
        stats.put("totalSales", totalSales);

        // 准备最终返回的数据结构
        Map<String, Object> data = new HashMap<>();
        data.put("itemStatistics", stats);

        // 构造成功返回的响应实体
        return createResponse(HttpStatus.OK, "菜品统计信息获取成功", data);
    }

    /**
     * 获取所有菜品的统计信息，包括评价和销量。
     *
     * @return ResponseEntity<Map<String, Object>> 返回一个包含所有菜品统计信息的响应实体，
     *         如果查询成功，返回状态码为200和统计数据。
     *         响应实体中包含的统计数据有：菜品ID、名称、描述、图片URL、类别、小份价格、大份价格、库存，
     *         平均评分、评价数量以及销量。
     */
    @GetMapping("/menu-items")
    public ResponseEntity<Map<String, Object>> getMenuItemsStatistics() {
        // 获取所有菜品信息
        List<MenuItem> menuItems = menuItemService.list();

        // 获取所有已完成的订单，用于后续计算销量
        List<Order> completedOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatus.COMPLETED.toString())
                .list();

        // 获取所有订单详情，用于统计每个菜品的销量
        List<OrderDetail> orderDetails = orderDetailService.list();

        // 统计每个菜品的评价和销量信息
        List<Map<String, Object>> menuItemsStats = menuItems.stream().map(menuItem -> {
            Map<String, Object> stats = new HashMap<>();
            // 菜品基本信息
            stats.put("itemId", menuItem.getItemId());
            stats.put("name", menuItem.getName());
            stats.put("description", menuItem.getDescription());
            stats.put("imageUrl", menuItem.getImageUrl());
            stats.put("category", menuItem.getCategory());
            stats.put("smallSizePrice", menuItem.getSmallSizePrice());
            stats.put("largeSizePrice", menuItem.getLargeSizePrice());
            stats.put("sizeStock", menuItem.getSizeStock());

            // 计算平均评分
            QueryWrapper<Review> avgWrapper = Wrappers.query();
            avgWrapper.select("AVG(rating) as avgRating")
                    .eq("item_id", menuItem.getItemId());
            Map<String, Object> avgResult = reviewService.getMap(avgWrapper);
            BigDecimal avgRatingDecimal = avgResult != null ? (BigDecimal) avgResult.get("avgRating") : BigDecimal.ZERO;
            Double averageRating = avgRatingDecimal.doubleValue();
            stats.put("averageRating", averageRating);

            // 统计该菜品的评价数量
            long totalReviews = reviewService.lambdaQuery()
                    .eq(Review::getItemId, menuItem.getItemId())
                    .count();
            stats.put("totalReviews", totalReviews);

            // 获取该菜品的所有评价
            List<Review> reviews = reviewService.lambdaQuery()
                    .eq(Review::getItemId, menuItem.getItemId())
                    .list();
            List<Map<String, Object>> reviewDetails = reviews.stream().map(review -> {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("reviewId", review.getReviewId());
                reviewData.put("userId", review.getUserId());
                reviewData.put("itemId", review.getItemId());
                reviewData.put("rating", review.getRating());
                reviewData.put("comment", review.getComment());
                reviewData.put("reviewTime", review.getReviewTime());
                return reviewData;
            }).collect(Collectors.toList());
            stats.put("reviews", reviewDetails);

            // 统计销量，筛选出对应菜品且订单已完成的订单详情，然后累加销量
            long totalSales = orderDetails.stream()
                    .filter(orderDetail -> orderDetail.getItemId().equals(menuItem.getItemId()) &&
                            completedOrders.stream()
                                    .anyMatch(order -> order.getOrderId().equals(orderDetail.getOrderId())))
                    .mapToLong(OrderDetail::getQuantity)
                    .sum();
            stats.put("totalSales", totalSales);

            return stats;
        }).collect(Collectors.toList());

        // 准备最终返回的数据结构
        Map<String, Object> data = new HashMap<>();
        data.put("menuItemsStatistics", menuItemsStats);

        // 构造成功返回的响应实体
        return createResponse(HttpStatus.OK, "所有菜品统计信息获取成功", data);
    }


}
