package com.example.backend.controller;

import com.example.backend.entity.Coupon;
import com.example.backend.entity.UserCoupon;
import com.example.backend.service.ICouponService;
import com.example.backend.service.IMenuItemService;
import com.example.backend.service.IUserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.backend.entity.Roles.ADMIN;

/**
 * @author zhouhaoran
 * @date 2024/5/15
 * @project Backend
 */
@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    @Autowired
    ICouponService couponService;

    @Autowired
    IUserCouponService userCouponService;

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }


    /**
     * 创建优惠券
     *
     * @param coupon 包含优惠券信息的请求体，通过RequestBody接收
     * @return 返回一个响应实体，包含创建成功后的优惠券信息和HTTP状态码
     */
    @Secured(ADMIN)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCoupon(@RequestBody Coupon coupon,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 检查优惠券参数
        if (coupon == null || coupon.getDiscount() == null || coupon.getExpirationDate() == null || coupon.getMinPurchase() == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的优惠券参数", null);
        }

        // 保存优惠券到服务端
        couponService.save(coupon);

        // 构造并返回创建成功的响应实体
        return createResponse(HttpStatus.CREATED, "优惠券创建成功", coupon);
    }


    /**
     * 更新现有的优惠券。
     *
     * @param couponId 优惠券ID，通过路径变量传递。
     * @param coupon 更新后的优惠券对象，通过RequestBody接收前端传来的数据。
     * @return 返回响应实体，包含更新后的优惠券对象和状态码200 OK。
     */
    @Secured(ADMIN)
    @PutMapping("/{couponId}")
    public ResponseEntity<Map<String, Object>> updateCoupon(@PathVariable Integer couponId, @RequestBody Coupon coupon,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 检查优惠券参数
        if (coupon == null || coupon.getDiscount() == null || coupon.getExpirationDate() == null || coupon.getMinPurchase() == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的优惠券参数", null);
        }

        // 检查优惠券是否存在
        if (couponService.getById(couponId) == null) {
            return createResponse(HttpStatus.NOT_FOUND, "优惠券未找到", null);
        }

        // 设置优惠券ID
        coupon.setCouponId(couponId);

        // 更新优惠券信息
        boolean updated = couponService.updateById(coupon);
        if (updated) {
            Coupon updatedCoupon = couponService.getById(couponId);
            return createResponse(HttpStatus.OK, "优惠券更新成功", updatedCoupon);
        } else {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "优惠券更新失败", null);
        }
    }

    /**
     * 删除指定的优惠券。
     *
     * @param couponId 优惠券ID，通过路径变量传递。
     * @return 返回响应实体，包含状态码200 OK表示删除成功，404 Not Found表示优惠券未找到。
     */
    @Secured(ADMIN)
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Map<String, Object>> deleteCoupon(@PathVariable Integer couponId,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        if (couponId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的优惠券ID", null);
        }

        boolean removed = couponService.removeById(couponId);
        if (removed) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("couponId", couponId);
            return createResponse(HttpStatus.OK, "优惠券删除成功", responseData);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "优惠券未找到", null);
        }
    }

    /**
     * 根据优惠券ID获取优惠券详情。
     *
     * @param couponId 优惠券ID，通过路径变量传递。
     * @return 返回响应实体，包含优惠券对象和状态码200 OK，或状态码404 Not Found表示未找到。
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<Map<String, Object>> getCouponById(@PathVariable Integer couponId,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        if (couponId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的优惠券ID", null);
        }

        Coupon coupon = couponService.getById(couponId);
        if (coupon != null) {
            return createResponse(HttpStatus.OK, "查询成功", coupon);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "优惠券未找到", null);
        }
    }

    /**
     * 获取所有有效的优惠券。
     *
     * @return 返回响应实体，包含所有有效的优惠券列表和状态码200 OK。
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveCoupons(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        List<Coupon> activeCoupons = couponService.lambdaQuery()
                .eq(Coupon::getIsActive, true)
                .gt(Coupon::getExpirationDate, new Date())
                .list();

        if (activeCoupons.isEmpty()) {
            return createResponse(HttpStatus.NOT_FOUND, "没有有效的优惠券", null);
        }

        return createResponse(HttpStatus.OK, "查询成功", activeCoupons);
    }


    /**
     * 批量创建优惠券
     *
     * @param coupons 包含多个优惠券信息的请求体，通过RequestBody接收
     * @return 返回一个响应实体，包含创建成功后的优惠券信息和HTTP状态码
     */
    @Secured(ADMIN)
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createCouponsBatch(@RequestBody List<Coupon> coupons,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        if (coupons == null || coupons.isEmpty()) {
            return createResponse(HttpStatus.BAD_REQUEST, "优惠券列表不能为空", null);
        }

        for (Coupon coupon : coupons) {
            if (coupon.getDiscount() == null || coupon.getExpirationDate() == null || coupon.getMinPurchase() == null) {
                return createResponse(HttpStatus.BAD_REQUEST, "优惠券参数无效", null);
            }
        }

        couponService.saveBatch(coupons);
        return createResponse(HttpStatus.CREATED, "优惠券批量创建成功", coupons);
    }

    /**
     * 获取所有未领取的优惠券
     *
     * @param authentication 当前用户的认证信息，用于权限验证
     * @return 返回一个响应实体，包含所有未领取的优惠券信息和HTTP状态码。如果用户未认证，返回401状态码；如果查询成功，返回200状态码和未领取的优惠券列表。
     */
    @GetMapping("/unclaimed")
    public ResponseEntity<Map<String, Object>> getUnclaimedCoupons(Authentication authentication) {
        // 权限验证：检查用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 查询所有未领取的优惠券逻辑
        // 首先获取所有优惠券，然后通过lambda查询表达式过滤出用户未领取的优惠券
        List<Coupon> unclaimedCoupons = couponService.list().stream()
                .filter(coupon -> userCouponService.lambdaQuery().eq(UserCoupon::getCouponId, coupon.getCouponId()).count() == 0)
                .collect(Collectors.toList());

        // 构造并返回响应实体，包含查询结果
        return createResponse(HttpStatus.OK, "查询成功", unclaimedCoupons);
    }


}
