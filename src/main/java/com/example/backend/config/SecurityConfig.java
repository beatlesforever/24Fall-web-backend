package com.example.backend.config;

import com.example.backend.service.IUserService;
import com.example.backend.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private IUserService userService;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    /**
     * 创建并配置JwtAuthenticationFilter Bean。
     * 这个过滤器用于处理JWT认证请求，会拦截特定的URL请求进行认证处理。
     *
     * @return JwtAuthenticationFilter 认证过滤器实例
     * @throws Exception 如果配置过程中发生错误，则抛出异常
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authenticationManagerBean(), userService);
        filter.setFilterProcessesUrl("/api/users/login"); // 设置过滤器处理的特定URL，用于用户登录验证
        return filter;
    }

    /**
     * 配置HTTP安全设置，以定义应用程序的 seguridad 规则。
     *
     * @param http 用于配置HttpSecurity的对象。
     * @throws Exception 如果在配置过程中发生错误。
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用CSRF保护，适用于无状态API
                .authorizeRequests()
                .antMatchers("/api/users/login", "/api/users/register", "/h2-console/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(jwtAuthenticationFilter())
                .addFilterBefore(new JwtAuthorizationFilter(userService), UsernamePasswordAuthenticationFilter.class);

        // 允许H2控制台的跨源请求
        http.headers().frameOptions().sameOrigin();

        // 启用CORS支持
        http.cors();
    }

    /**
     * 配置全局CORS策略的函数。
     * 这个函数没有参数。
     * @return 返回一个WebMvcConfigurer实例，用来配置CORS跨域资源共享规则。
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * 向Spring MVC添加CORS映射的函数。
             * 这个函数接收一个CorsRegistry实例，用来注册CORS映射。
             * @param registry CorsRegistry实例，用于添加CORS映射。
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 为所有路径添加CORS映射，并配置相关选项
                registry.addMapping("/**")
                        .allowedOrigins("*") // 允许指定域访问
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                        .allowedHeaders("*") // 允许所有请求头
                        .allowCredentials(true) // 允许凭证（cookies）
                        .maxAge(3600); // 设置预检请求的缓存时间（1小时）
            }
        };
    }

}
