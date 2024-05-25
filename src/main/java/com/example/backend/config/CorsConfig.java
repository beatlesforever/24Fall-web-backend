package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhouhaoran
 * @date 2024/5/25
 * @project Backend
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS（跨源资源共享）规则的Bean。
     * 这个方法创建一个WebMvcConfigurer的匿名子类，重写了addCorsMappings方法，用来定义具体的CORS规则。
     *
     * @return 返回一个配置了CORS规则的WebMvcConfigurer实例。
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * 向Spring MVC添加CORS映射，允许特定的跨域请求。
             *
             * @param registry CORS注册表，用于配置CORS规则。
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 为所有路径添加CORS映射，并配置相关选项
                registry.addMapping("/**")
                        // 允许来自https://order.lc-0.cn的跨域请求
                        .allowedOrigins("https://order.lc-0.cn")
                        // 允许GET、POST、PUT、DELETE和OPTIONS方法的跨域请求
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // 允许所有请求头
                        .allowedHeaders("*")
                        // 允许浏览器发送凭证（如cookies）
                        .allowCredentials(true)
                        // 设置预检请求的缓存时间（单位：秒）
                        .maxAge(3600);
            }
        };
    }

}
