package com.example.backend.config;

import com.example.backend.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserServiceImpl userService;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 如果您的API是无状态的，考虑禁用CSRF保护
                .authorizeRequests()
                // 允许对登录和注册API的无条件访问
                .antMatchers( "/api/users/login", "/api/users/register","/h2-console/**").permitAll()
                // 指定其他路径下的接口不需要认证即可访问（如果有需要）
                .antMatchers("/api/public/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManager())) // 添加自定义JWT认证过滤器
                .addFilterBefore(new JwtAuthorizationFilter(userService), UsernamePasswordAuthenticationFilter.class);

        // 添加JWT支持或其他认证/授权机制的配置...
        // 禁用CSRF，允许使用H2控制台
        http.csrf().disable();

        // 允许同源到h2控制台的请求。
        http.headers().frameOptions().sameOrigin();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
