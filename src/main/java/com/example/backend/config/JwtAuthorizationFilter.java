package com.example.backend.config;

import com.example.backend.service.impl.UserServiceImpl; // 导入用户服务实现
import io.jsonwebtoken.Claims; // JWT的Claims
import io.jsonwebtoken.Jwts; // JWT的工具类
import org.springframework.security.authentication.AuthenticationManager; // 认证管理器
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 认证令牌
import org.springframework.security.core.context.SecurityContextHolder; // 安全上下文持有者
import org.springframework.security.core.userdetails.UserDetails; // 用户详细信息
import org.springframework.web.filter.OncePerRequestFilter; // 每请求一次过滤器

import javax.servlet.FilterChain; // 过滤器链
import javax.servlet.ServletException; // Servlet异常
import javax.servlet.http.HttpServletRequest; // HTTP请求
import javax.servlet.http.HttpServletResponse; // HTTP响应
import java.io.IOException; // IO异常

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final UserServiceImpl userService; // 用户服务

    // 构造函数，注入用户服务
    public JwtAuthorizationFilter(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        // 从请求头中获取Authorization字段
        String header = request.getHeader("Authorization");
        // 如果头部为空或不是Bearer类型，则继续过滤链
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 去除Bearer前缀，获取真正的token字符串
        String token = header.replace("Bearer ", "");
        try {
            // 解析JWT
            Claims claims = Jwts.parser()
                    .setSigningKey("SecretKey") // 设置解析JWT的密钥
                    .parseClaimsJws(token) // 解析JWT
                    .getBody();

            // 从JWT中获取电话号码
            String phone = claims.getSubject();
            if (phone != null) {

                // 根据电话号码加载用户详细信息
                UserDetails userDetails = userService.loadUserByUsername(phone); // 使用新的方法名

                // 创建认证令牌，并设置到安全上下文中
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 如果认证失败，清除上下文
            SecurityContextHolder.clearContext();
        }

        // 继续执行过滤链
        chain.doFilter(request, response);
    }
}
