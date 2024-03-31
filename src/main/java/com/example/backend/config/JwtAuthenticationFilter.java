package com.example.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper; // 导入Jackson库来解析JSON
import com.example.backend.dto.UserLoginDTO; // 导入用户登录DTO
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager; // Spring Security的认证管理器
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 用户名密码认证令牌
import org.springframework.security.core.Authentication; // Spring Security的认证接口
import org.springframework.security.core.AuthenticationException; // 认证异常
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 用户名密码认证过滤器

import javax.servlet.FilterChain; // 过滤器链
import javax.servlet.ServletException; // Servlet异常
import javax.servlet.http.HttpServletRequest; // HTTP请求
import javax.servlet.http.HttpServletResponse; // HTTP响应
import java.io.IOException; // IO异常
import java.util.Date; // 日期类

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager; // 认证管理器

    // 构造函数，注入AuthenticationManager
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/api/users/login"); // 设置自定义登录路径
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            // 将请求中的JSON转换成UserLoginDTO对象
            UserLoginDTO credentials = new ObjectMapper()
                    .readValue(request.getInputStream(), UserLoginDTO.class);

            // 创建认证令牌
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    credentials.getPhone(),
                    credentials.getPassword()
            );
            // 使用AuthenticationManager完成认证工作
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            // 如果出现异常，抛出运行时异常
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // 认证成功后，生成JWT
        String token = Jwts.builder()
                .setSubject(((org.springframework.security.core.userdetails.User) authResult.getPrincipal()).getUsername())
                //: authResult.getPrincipal()调用获取了认证过程中生成的Principal对象。在Spring Security的上下文中，
                // Principal代表了经过认证的用户或实体的信息。通常情况下，这个对象是一个UserDetails实例，包含了用户的详细信息。
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 设置过期时间
                .signWith(SignatureAlgorithm.HS512, "SecretKey") // 设置签名算法和密钥
                .compact();

        // 将JWT作为响应头返回
        response.addHeader("Authorization", "Bearer " + token);
    }
}
