package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.User;
import com.example.backend.dto.UserLoginDTO;
import com.example.backend.dto.UserRegisterDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;

/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */
public interface IUserService extends IService<User> {
    boolean register(UserRegisterDTO userRegisterDTO);
    User findById(Integer userId);
    UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException;
    boolean validateUser(UserLoginDTO userLoginDTO);
    boolean recharge(String phone, BigDecimal amount);
    boolean placeOrder(String phone, BigDecimal orderAmount);
    User findByPhone(String phone);
}
