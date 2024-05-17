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
import java.util.List;
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
     * 创建一个包含状态码、消息和数据的响应实体。
     *
     * @param status HTTP状态码，代表响应的状态。
     * @param message 响应消息，用于描述响应的详细信息。
     * @param data 响应数据，实际返回给客户端的内容。
     * @return ResponseEntity<Map<String, Object>> 一个包含状态码、消息和数据的响应实体。
     */
    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        // 初始化响应体，用于存放状态码、消息和数据
        Map<String, Object> responseBody = new HashMap<>();
        // 设置响应状态，包括状态码和状态码描述
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        // 设置响应消息
        responseBody.put("message", message);
        // 设置响应数据
        responseBody.put("data", data);
        // 构造并返回响应实体
        return new ResponseEntity<>(responseBody, status);
    }


    /**
     * 用户注册接口。接收用户注册请求，处理用户注册逻辑。
     *
     * @param userRegisterDTO 包含用户注册信息的数据传输对象, 通常包括用户名、密码、联系方式等。
     *                        用户注册信息必须包含一个有效的角色类型（管理员或普通用户）。
     * @return 返回一个响应实体，如果注册成功，包含成功的消息；如果注册失败，包含失败的原因。
     *         响应实体使用HTTP状态码来表示操作的成功或失败。
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserRegisterDTO userRegisterDTO) {
        // 设置默认角色为 customer
        // 调用userService完成用户注册逻辑，并根据结果返回相应的响应实体
        boolean isRegistered = userService.register(userRegisterDTO, CUSTOMER);

        if (isRegistered) {
            return createResponse(HttpStatus.OK, "用户注册成功", null);
        } else {
            // 注册失败，可能是由于手机号已存在或其他原因
            return createResponse(HttpStatus.BAD_REQUEST, "注册失败，可能是由于手机号已存在或其他原因", null);
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
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return createResponse(HttpStatus.UNAUTHORIZED, "未认证的用户", null);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String phone = userDetails.getUsername();
        User user = userService.findByPhone(phone);
        System.out.println(user);
        if (user != null) {
            return createResponse(HttpStatus.OK, "用户信息获取成功", user);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "用户信息未找到", null);
        }
    }

    /**
     * 新增充值接口
     *
     * @param authentication 用户认证信息，用于确认请求用户的身份
     * @param amountMap 包含充值金额的Map，其中"amount"键对应的值为充值的金额
     * @return ResponseEntity<?> 根据充值结果返回不同的响应，包括成功或失败的信息
     */
    @PostMapping("/recharge")
    public ResponseEntity<Map<String, Object>> recharge(Authentication authentication, @RequestBody Map<String, BigDecimal> amountMap) {
        // 检查用户认证信息是否合法
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            System.err.println("2222");
            return createResponse(HttpStatus.UNAUTHORIZED, "未认证的用户", null);
        }

        // 从认证信息中提取用户详情
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 提取充值金额，并检查其合法性
        BigDecimal amount = amountMap.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return createResponse(HttpStatus.BAD_REQUEST, "充值金额必须大于0", null);
        }

        // 执行用户充值操作，并根据结果返回相应的响应
        boolean success = userService.recharge(userDetails.getUsername(), amount);
        return success ? createResponse(HttpStatus.OK, "充值成功", null) : createResponse(HttpStatus.BAD_REQUEST, "充值失败", null);
    }


    /**
     * 新增下单接口
     *
     * @param authentication 用户认证信息，用于确认请求用户的权限和身份
     * @param orderAmountMap 包含订单金额的Map，其中"orderAmount"键对应的值为订单金额
     * @return ResponseEntity<?> 根据订单创建的结果返回不同的状态和信息：成功创建返回200 OK，包含成功信息；未认证用户返回401 Unauthorized；订单金额不合法返回400 Bad Request。
     */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> placeOrder(Authentication authentication, @RequestBody Map<String, BigDecimal> orderAmountMap) {
        // 检查用户认证信息是否合法
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return createResponse(HttpStatus.UNAUTHORIZED, "未认证的用户", null);
        }

        // 获取用户详细信息并验证订单金额
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        BigDecimal orderAmount = orderAmountMap.get("orderAmount");
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return createResponse(HttpStatus.BAD_REQUEST, "订单金额必须大于0", null);
        }

        // 尝试创建订单
        boolean success = userService.placeOrder(userDetails.getUsername(), orderAmount);
        // 根据订单创建结果返回相应信息
        return success ? createResponse(HttpStatus.OK, "订单创建成功，余额已扣除", null) : createResponse(HttpStatus.BAD_REQUEST, "订单创建失败，可能是余额不足", null);
    }



    /**
     * 重置用户密码的接口。
     *
     * @param authentication 当前用户的认证信息，用于确认用户身份和权限。
     * @param passwordMap 包含新密码的Map，"newPassword"键对应的值为新密码。
     * @return 返回一个ResponseEntity对象，包含密码重置的结果状态码、消息和数据。
     */
    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> resetPassword(Authentication authentication, @RequestBody Map<String, String> passwordMap) {
        // 检查用户是否已认证，以及认证信息中是否包含UserDetails对象
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return createResponse(HttpStatus.UNAUTHORIZED, "未认证的用户", null);
        }

        // 从请求体中获取新密码
        String newPassword = passwordMap.get("newPassword");
        // 将认证信息中的UserDetails转换成具体的用户详情对象
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 调用服务层方法，尝试重置密码
        boolean success = userService.resetPassword(userDetails.getUsername(), newPassword);
        // 根据重置密码的结果，返回相应的ResponseEntity
        return success ? createResponse(HttpStatus.OK, "密码重置成功", null) : createResponse(HttpStatus.BAD_REQUEST, "密码重置失败", null);
    }

    /**
     * 获取所有用户信息。
     * 仅管理员角色（ADMIN）有权限访问此接口。
     *
     * @param authentication 用户的认证信息，用于验证用户身份。
     * @return 返回一个响应实体，包含所有用户的列表。如果用户未认证或没有权限，返回401或403状态码。
     */
    @Secured(ADMIN)
    @GetMapping
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        List<User> users = userService.list();
        return createResponse(HttpStatus.OK, "获取所有用户成功", users);
    }


}
