package com.example.backend.controller;

import com.example.backend.dto.UserLoginDTO;
import com.example.backend.dto.UserRegisterDTO;
import com.example.backend.entity.User;
import com.example.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
/**
 * 用户控制器，处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/users") // 定义类级别的请求路径
public class UserController {
    @Autowired
    IUserService userService;

    /**
     * 用户注册接口。接收用户注册请求，处理用户注册逻辑。
     *
     * @param userRegisterDTO 包含用户注册信息的数据传输对象，通常包括用户名、密码、联系方式等。
     * @return 如果注册成功，返回注册成功的消息的响应实体；如果注册失败，返回失败原因的响应实体。
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO userRegisterDTO) {
        // 尝试使用提供的用户信息进行注册
        boolean isRegistered = userService.register(userRegisterDTO);
        if (isRegistered) {
            // 注册成功，返回成功消息
            return ResponseEntity.ok().body("用户注册成功");
        } else {
            // 注册失败，返回失败消息
            return ResponseEntity.badRequest().body("注册失败，可能是由于手机号已存在或其他原因");
        }
    }

    /**
     * 处理用户登录请求。
     *
     * @param userLoginDTO 包含用户登录信息的数据传输对象，通常包含手机号和密码等认证信息。
     * @return 如果用户验证成功，返回一个表示成功的信息，否则返回一个表示失败的信息。
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDTO userLoginDTO) {
        // 验证用户是否存在，实际的认证过程由JwtAuthenticationFilter处理
        boolean isValidUser = userService.validateUser(userLoginDTO);
        if (isValidUser) {
            // 用户验证成功。由于JwtAuthenticationFilter会自动处理认证并添加token到响应头部，这里不需要直接返回token
            return ResponseEntity.ok().body("登录请求已提交，请检查响应头部的Authorization字段获取token。");
        } else {
            // 用户验证失败，返回登录失败信息
            return ResponseEntity.badRequest().body("登录失败，手机号或密码错误");
        }
    }

    /**
     * 通过GET请求获取当前用户的信息。
     *
     * @param authentication 当前请求的认证信息，用于获取用户身份。
     * @return 如果用户认证成功且提供了有效的用户信息，则返回该用户的信息实体；否则，返回错误信息。
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        // 验证用户认证信息是否合法
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String phone = userDetails.getUsername(); // 获取电话号码
        User user = userService.findByPhone(phone); // 使用电话号码获取 User 实体
        if (user != null) {
            return ResponseEntity.ok(user); // 返回用户信息
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户信息未找到");

    }

    /**
     * 新增充值接口
     *
     * @param authentication 用户认证信息，用于确认请求用户的身份
     * @param amountMap 包含充值金额的Map，其中"amount"键对应的值为充值的金额
     * @return ResponseEntity<?> 根据充值结果返回不同的响应，包括成功或失败的信息
     */
    @PostMapping("/recharge")
    public ResponseEntity<?> recharge(Authentication authentication, @RequestBody Map<String, BigDecimal> amountMap) {
        // 检查用户是否已认证，以及认证信息是否有效
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        // 从认证信息中提取用户详情
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 提取充值金额，并进行合法性检查
        BigDecimal amount = amountMap.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("充值金额必须大于0");
        }
        // 调用服务层方法执行充值操作，并根据结果返回相应的响应
        boolean success = userService.recharge(userDetails.getUsername(), amount);
        return success ? ResponseEntity.ok().body("充值成功") : ResponseEntity.badRequest().body("充值失败");
    }


    /**
     * 新增下单接口
     *
     * @param authentication 用户认证信息，用于确认请求用户的权限和身份
     * @param orderAmountMap 包含订单金额的Map，其中"orderAmount"键对应的值为订单金额
     * @return ResponseEntity<?> 根据订单创建的结果返回不同的状态和信息：成功创建返回200 OK，包含成功信息；未认证用户返回401 Unauthorized；订单金额不合法返回400 Bad Request。
     */
    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(Authentication authentication, @RequestBody Map<String, BigDecimal> orderAmountMap) {
        // 验证用户认证信息是否合法
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 提取并验证订单金额
        BigDecimal orderAmount = orderAmountMap.get("orderAmount");
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("订单金额必须大于0");
        }
        // 尝试创建订单
        boolean success = userService.placeOrder(userDetails.getUsername(), orderAmount);
        // 根据订单创建结果返回相应信息
        return success ? ResponseEntity.ok().body("订单创建成功，余额已扣除") : ResponseEntity.badRequest().body("订单创建失败，可能是余额不足");
    }
}
