package com.example.backend.controller;

import com.example.backend.dto.UserLoginDTO;
import com.example.backend.dto.UserRegisterDTO;
import com.example.backend.entity.Roles;
import com.example.backend.entity.User;
import com.example.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.example.backend.entity.Roles.ADMIN;
import static com.example.backend.entity.Roles.CUSTOMER;

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
        // 校验role字段是否有效
        if (!ADMIN.equals(userRegisterDTO.getRole()) && !Roles.CUSTOMER.equals(userRegisterDTO.getRole())) {
            return ResponseEntity.badRequest().body("无效的角色类型");
        }

        // 尝试使用提供的用户信息进行注册
        boolean isRegistered = userService.register(userRegisterDTO, userRegisterDTO.getRole());

        if (isRegistered) {
            // 注册成功，返回成功消息
            return ResponseEntity.ok().body("用户注册成功");
        } else {
            // 注册失败，返回失败消息
            return ResponseEntity.badRequest().body("注册失败，可能是由于手机号已存在或其他原因");
        }
    }

    /**
     * 获取用户信息
     * 本接口使用GET请求访问路径/info
     * 通过Spring Security的Authentication参数获取用户认证信息，并从中提取用户电话号码，
     * 进而查询用户详细信息。
     *
     * @param authentication 用户的认证信息，用于验证用户身份和权限。
     * @return ResponseEntity<?> 根据操作结果返回不同的响应体。如果用户认证失败或未找到相关用户信息，
     *         返回状态码为401或404；否则，返回状态码为200和查询到的User对象。
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        // 验证用户认证信息是否合法
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String phone = userDetails.getUsername(); // 获取电话号码作为用户名
        User user = userService.findByPhone(phone); // 根据电话号码查找用户
        if (user != null) {
            return ResponseEntity.ok(user); // 用户存在，返回用户信息
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


    @PostMapping("/password")
    public ResponseEntity<?> resetPassword(Authentication authentication,
                                           @RequestBody Map<String, String> passwordMap) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未认证的用户");
        }

        String newPassword = passwordMap.get("newPassword");
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        boolean success = userService.resetPassword(userDetails.getUsername(), newPassword);
        return success ? ResponseEntity.ok( "密码重置成功")
                : ResponseEntity.badRequest().body("密码重置失败");
    }

}
