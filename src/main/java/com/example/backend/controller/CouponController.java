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
    private static final Random RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 4;

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 生成唯一的4位数字优惠券代码
     * 该函数没有参数。
     * @return 返回生成的唯一4位数字优惠券代码。代码是4位数字格式，通过生成随机代码并检查其唯一性来确保唯一。
     */
    private String generateUniqueCode() {
        String code;
        do {
            // 生成一个随机的4位数字优惠券代码
            code = generateRandomCode();
        // 检查生成的代码是否已存在，如果存在，则重新生成
        } while (couponService.lambdaQuery().eq(Coupon::getCode, code).count() > 0);
        return code;
    }


    /**
     * 生成随机的4位数字优惠券代码
     *
     * @return 返回生成的随机4位数字优惠券代码。代码由0-9的数字组成，长度为4。
     */
    private String generateRandomCode() {
        // 初始化一个StringBuilder，长度为CODE_LENGTH（假设CODE_LENGTH为4）
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        // 循环生成随机数字并添加到StringBuilder中，直到达到指定长度
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10)); // 生成并添加一个0-9之间的随机数字
        }
        // 将StringBuilder内容转换为String并返回
        return code.toString();
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

        // 生成唯一的4位数字优惠券代码
        coupon.setCode(generateUniqueCode());

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
     * 使用优惠券代码获取优惠券详情。
     *
     * @param code 优惠券代码，通过请求参数传递。
     * @return 返回响应实体，包含优惠券对象和状态码200 OK，或状态码404 Not Found表示未找到。
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getCouponByCode(@PathVariable String code,Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        if (code == null || code.trim().isEmpty()) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的优惠券代码", null);
        }

        Coupon coupon = couponService.lambdaQuery().eq(Coupon::getCode, code).one();
        if (coupon != null) {
            return createResponse(HttpStatus.OK, "查询成功", coupon);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "优惠券未找到", null);
        }
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
            coupon.setCode(generateUniqueCode());
        }

        couponService.saveBatch(coupons);
        return createResponse(HttpStatus.CREATED, "优惠券批量创建成功", coupons);
    }

}
