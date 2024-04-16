package com.example.backend.controller;

import com.example.backend.entity.Review;
import com.example.backend.service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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

        // 设置当前服务器时间为评价时间，确保评价时间的准确性
        review.setReviewTime(Timestamp.from(Instant.now()));

        // 保存评价到数据库，调用reviewService的save方法
        reviewService.save(review);
        // 返回评价添加成功的消息，并携带200 OK的状态码
        return ResponseEntity.ok("评价添加成功");
    }


    /**
     * 修改评价
     *
     * @param reviewId 评价的ID，用于指定要修改的评价
     * @param review 包含修改后评价信息的对象
     * @param authentication 当前用户的认证信息，用于权限验证
     * @return 如果用户未认证，返回401状态码和"未认证的用户"消息；如果评价更新成功，返回200状态码和"评价更新成功"消息；如果评价未找到，返回404状态码。
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Integer reviewId, @RequestBody Review review, Authentication authentication) {
        // 检查用户是否认证
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        review.setReviewId(reviewId);
        // 更新评价
        boolean updated = reviewService.updateById(review);
        // 根据更新结果返回相应的响应
        return updated ? ResponseEntity.ok("评价更新成功") : ResponseEntity.notFound().build();
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
        // 检查用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 根据评价ID删除评价
        boolean removed = reviewService.removeById(reviewId);
        // 删除成功返回200，失败返回404
        return removed ? ResponseEntity.ok("评价删除成功") : ResponseEntity.notFound().build();
    }


    /**
     * 根据菜品ID获取其所有评价
     *
     * @param itemId 菜品的ID，作为查询条件
     * @return 返回一个响应实体，包含指定菜品ID的所有评价列表
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<Review>> getReviewsByItemId(@PathVariable Integer itemId) {
        // 使用lambda查询方式，根据菜品ID查询所有的评价
        List<Review> reviews = reviewService.lambdaQuery().eq(Review::getItemId, itemId).list();
        return ResponseEntity.ok(reviews);
    }

    /**
     * 根据用户ID获取该用户的所有评价
     *
     * @param userId 用户的ID，作为查询条件
     * @return 返回一个响应实体，包含该用户的所有评价列表。如果查询成功，响应状态码为200 OK。
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable Integer userId) {
        // 通过用户ID查询该用户的所有评价
        List<Review> reviews = reviewService.lambdaQuery().eq(Review::getUserId, userId).list();
        return ResponseEntity.ok(reviews);
    }
}
