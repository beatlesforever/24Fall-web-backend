package com.example.backend.config;

import com.example.backend.service.IUserService;
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
    private final IUserService userService; // 使用接口而非具体实现

    // 构造函数，注入用户服务
    // 构造函数，注入用户服务
    public JwtAuthorizationFilter(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 处理过滤请求，用于JWT认证。
     * 从请求头中提取Authorization字段，验证其是否为Bearer类型的JWT令牌。
     * 如果是，解析JWT以获取用户信息，并进行认证。
     * 如果不是，或解析失败，则继续过滤链。
     *
     * @param request  HttpServletRequest对象，代表客户端的HTTP请求。
     * @param response HttpServletResponse对象，用于向客户端发送HTTP响应。
     * @param chain    FilterChain对象，代表过滤链，用于将请求传递给下一个过滤器或目标。
     * @throws IOException 如果处理请求时发生IO错误。
     * @throws ServletException 如果处理请求时发生Servlet相关错误。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        // 尝试从请求头获取Authorization信息
        String header = request.getHeader("Authorization");
        // 如果没有或不是Bearer格式的令牌，则直接通过过滤链
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 提取Bearer之后的token字符串
        String token = header.replace("Bearer ", "");

        try {
            // 解析JWT token
            Claims claims = Jwts.parser()
                    .setSigningKey("SecretKey") // 使用预定义的密钥解析
                    .parseClaimsJws(token) // 解析JWT并获取claims
                    .getBody();

            // 从JWT中提取用户姓名
            String phone = claims.getSubject();
            if (phone != null) {

                UserDetails userDetails = userService.loadUserByUsername(phone); // 不得不叫这个名字，但是确实是手机号码

                // 创建并设置认证信息到安全上下文
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 认证失败，清除安全上下文
            SecurityContextHolder.clearContext();
        }

        // 继续处理请求链
        chain.doFilter(request, response);
    }

}
