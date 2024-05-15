package com.example.backend.controller;

import com.example.backend.entity.Coupon;
import com.example.backend.entity.UserCoupon;
import com.example.backend.service.ICouponService;
import com.example.backend.service.IUserCouponService;
import com.example.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;

/**
 * @author zhouhaoran
 * @date 2024/5/15
 * @project Backend
 */
@RestController
@RequestMapping("/api/user/coupon")
public class UserCouponController {

    @Autowired
    IUserCouponService userCouponService;

    @Autowired
    ICouponService couponService;

    @Autowired
    IUserService userService;


    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 分配优惠券给用户
     *
     * @param userCoupon 包含用户ID和优惠券ID的请求体，通过RequestBody接收
     * @return 返回响应实体，包含分配成功的信息和HTTP状态码
     */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignCouponToUser(@RequestBody UserCoupon userCoupon,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        // 检查用户和优惠券ID是否有效
        if (userCoupon.getUserId() == null || userCoupon.getCouponId() == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "用户ID和优惠券ID不能为空", null);
        }

        // 检查用户是否存在
        if (userService.getById(userCoupon.getUserId()) == null) {
            return createResponse(HttpStatus.NOT_FOUND, "用户未找到", null);
        }

        // 检查优惠券是否存在且是否有效
        Coupon coupon = couponService.getById(userCoupon.getCouponId());
        if (coupon == null || !coupon.getIsActive() || coupon.getExpirationDate().before(new Date())) {
            return createResponse(HttpStatus.NOT_FOUND, "优惠券未找到或不可用", null);
        }

        // 检查该优惠券是否已经被领取
        List<UserCoupon> existingUserCoupons = userCouponService.lambdaQuery()
                .eq(UserCoupon::getCouponId, userCoupon.getCouponId())
                .list();
        if (!existingUserCoupons.isEmpty()) {
            return createResponse(HttpStatus.BAD_REQUEST, "该优惠券已被领取", null);
        }

        // 设置优惠券状态为未使用
        userCoupon.setIsUsed(false);

        // 保存用户优惠券信息
        userCouponService.save(userCoupon);

        // 返回成功响应
        return createResponse(HttpStatus.CREATED, "优惠券分配成功", userCoupon);
    }

    /**
     * 获取用户的所有优惠券
     *
     * @param userId 用户ID，通过路径变量传递
     * @return 返回响应实体，包含用户的所有优惠券信息和HTTP状态码
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserCoupons(@PathVariable Integer userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        if (userId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的用户ID", null);
        }

        // 检查用户是否存在
        if (userService.getById(userId) == null) {
            return createResponse(HttpStatus.NOT_FOUND, "用户未找到", null);
        }

        List<UserCoupon> userCoupons = userCouponService.lambdaQuery().eq(UserCoupon::getUserId, userId).list();

        // 返回成功响应
        return createResponse(HttpStatus.OK, "查询成功", userCoupons);
    }


    /**
     * 获取用户未使用的优惠券
     *
     * @param userId 用户ID，通过路径变量传递
     * @return 返回响应实体，包含用户未使用的优惠券信息和HTTP状态码
     */
    @GetMapping("/user/{userId}/unused")
    public ResponseEntity<Map<String, Object>> getUnusedUserCoupons(@PathVariable Integer userId,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        if (userId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的用户ID", null);
        }

        // 检查用户是否存在
        if (userService.getById(userId) == null) {
            return createResponse(HttpStatus.NOT_FOUND, "用户未找到", null);
        }

        List<UserCoupon> unusedUserCoupons = userCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getIsUsed, false)
                .list();

        // 返回成功响应
        return createResponse(HttpStatus.OK, "查询成功", unusedUserCoupons);
    }

    /**
     * 获取用户已使用的优惠券
     *
     * @param userId 用户ID，通过路径变量传递
     * @return 返回响应实体，包含用户已使用的优惠券信息和HTTP状态码
     */
    @GetMapping("/user/{userId}/used")
    public ResponseEntity<Map<String, Object>> getUsedUserCoupons(@PathVariable Integer userId,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        if (userId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的用户ID", null);
        }

        // 检查用户是否存在
        if (userService.getById(userId) == null) {
            return createResponse(HttpStatus.NOT_FOUND, "用户未找到", null);
        }

        List<UserCoupon> usedUserCoupons = userCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getIsUsed, true)
                .list();

        // 返回成功响应
        return createResponse(HttpStatus.OK, "查询成功", usedUserCoupons);
    }

}
