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
     * 这个方法定制了Spring Security的HttpSecurity配置，来适应应用的授权和安全需求。
     * 具体配置包括：禁用CSRF保护、配置哪些URL路径需要授权、哪些路径对所有请求开放、
     * 添加自定义的JWT认证过滤器以及配置CORS和frameOptions。
     *
     * @param http 用于配置HttpSecurity的对象。HttpSecurity是一个配置接口，用于定制请求过滤器、访问控制等安全设置。
     * @throws Exception 如果在配置过程中发生错误。任何配置错误都可能抛出异常。
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用CSRF保护，因为JWT认证不需要它
        http
                .csrf().disable()
                // 配置请求授权规则
                .authorizeRequests()
                // 指定不需要认证即可访问的路径
                .antMatchers("/api/users/login", "/api/users/register", "/h2-console/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                // 除上述路径外，其它所有请求都需要认证
                .anyRequest().authenticated()
                // 添加JWT认证过滤器
                .and()
                .addFilter(jwtAuthenticationFilter())
                // 在UsernamePasswordAuthenticationFilter之前添加JWT授权过滤器
                .addFilterBefore(new JwtAuthorizationFilter(userService), UsernamePasswordAuthenticationFilter.class);

        // 配置HTTP头，允许同源策略，支持CORS
        http.headers().frameOptions().sameOrigin();
        http.cors();
    }

}
