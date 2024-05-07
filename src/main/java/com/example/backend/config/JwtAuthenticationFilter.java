package com.example.backend.config;

import com.example.backend.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper; // 导入Jackson库来解析JSON
import com.example.backend.dto.UserLoginDTO; // 导入用户登录DTO
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager; // Spring Security的认证管理器
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 用户名密码认证令牌
import org.springframework.security.core.Authentication; // Spring Security的认证接口
import org.springframework.security.core.AuthenticationException; // 认证异常
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 用户名密码认证过滤器

import javax.servlet.FilterChain; // 过滤器链
import javax.servlet.ServletException; // Servlet异常
import javax.servlet.http.HttpServletRequest; // HTTP请求
import javax.servlet.http.HttpServletResponse; // HTTP响应
import java.io.IOException; // IO异常
import java.util.Date; // 日期类
import java.util.HashMap;
import java.util.Map;
import com.example.backend.entity.User;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager; // 认证管理器，用于在Spring Security中进行认证操作
    private final IUserService userService;

    // 构造函数，通过参数注入AuthenticationManager
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, IUserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        setFilterProcessesUrl("/api/users/login");
        setAuthenticationManager(authenticationManager); // 显式设置 AuthenticationManager
    }

    /**
     * 尝试对用户进行认证。
     * 该方法会从HTTP请求中读取JSON格式的用户登录信息，然后使用这些信息创建一个认证令牌，并通过认证管理器来尝试进行认证。
     *
     * @param request HttpServletRequest对象，用于获取客户端请求的数据。
     * @param response HttpServletResponse对象，用于向客户端发送响应。
     * @return Authentication对象，表示认证的结果。如果认证成功，返回一个已认证的Authentication实例。
     * @throws AuthenticationException 如果认证过程中出现异常，则抛出此异常。
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            // 从HTTP请求中读取并解析JSON数据，将其转换成UserLoginDTO对象
            UserLoginDTO credentials = new ObjectMapper()
                    .readValue(request.getInputStream(), UserLoginDTO.class);

            // 创建认证令牌，此令牌尚未认证
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    credentials.getPhone(), // 使用用户提交的手机号作为认证主体（principal）
                    credentials.getPassword() // 使用用户提交的密码
            );

            // 使用认证管理器完成认证过程，如果认证成功，返回一个已认证的Authentication实例
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            // 在处理请求数据时发生IO异常，转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }


    /**
     * 当用户认证成功后调用的函数，用于生成并返回JWT（JSON Web Token）。
     * 此JWT用于后续请求的授权验证。
     *
     * @param request  HttpServletRequest对象，代表客户端的HTTP请求
     * @param response HttpServletResponse对象，用于向客户端发送HTTP响应
     * @param chain    FilterChain对象，用于继续过滤或终止过滤
     * @param authResult 认证结果对象，包含用户认证的信息
     * @throws IOException 如果发生I/O错误
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException{
        // 获取用户详细信息
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String phone = userDetails.getUsername();
        // 根据电话号码查找用户
        User user = userService.findByPhone(phone);
        if (user == null) {
            // 如果用户不存在，则抛出异常
            throw new UsernameNotFoundException("User not found with phone: " + phone);
        }

        // 创建JWT
        // 设置JWT主题(用户名), 权限信息, 过期时间, 签名算法和密钥
        String token = Jwts.builder()
                .setSubject(user.getPhone()) // JWT 中表示用户身份的字段，这里放的是手机号码
                .claim("roles", authResult.getAuthorities()) // 包含用户的角色和权限信息。
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // JWT 的有效期设置为当前时间之后的 24 小时
                .signWith(SignatureAlgorithm.HS512, "SecretKey") // 使用 HS512 算法和 "SecretKey" 作为密钥进行签名。
                .compact();

        // 将JWT作为HTTP响应头发送给客户端
        response.addHeader("Authorization", "Bearer " + token);

        // 构建登录成功后的响应体数据
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getName());
        data.put("phone", user.getPhone());
        data.put("balance", user.getBalance());
        data.put("role", user.getRole());

        // 构建最终发送给客户端的响应信息
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "200 OK");
        responseBody.put("data", data);
        responseBody.put("message", "用户登录成功！");

        // 将响应信息转换为JSON格式并写回响应流
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(responseBody);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

}
