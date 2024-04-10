package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.UserLoginDTO;
import com.example.backend.dto.UserRegisterDTO;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.IUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService, UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册用户。
     *
     * @param userRegisterDTO 用户注册数据传输对象，包含用户名、手机号和密码。
     * @return boolean 如果注册成功返回true，如果手机号已存在返回false。
     */
    @Override
    public boolean register(UserRegisterDTO userRegisterDTO, String role) {
        // 检查手机号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", userRegisterDTO.getPhone());
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            return false; // 手机号已存在
        }

        // 加密密码并保存用户信息
        User user = new User();
        user.setName(userRegisterDTO.getName());
        user.setPhone(userRegisterDTO.getPhone());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword())); // 加密密码
        user.setRegistrationDate(new Date());
        user.setBalance(java.math.BigDecimal.ZERO); // 设置初始余额为0
        user.setRole(role); // 设置用户角色
        userMapper.insert(user);
        return true;
    }

    /**
     * 验证用户登录信息的正确性。
     *
     * @param userLoginDTO 包含用户手机号和密码的登录数据传输对象。
     * @return 返回布尔值，如果用户存在且密码正确，则返回true；否则返回false。
     */
    @Override
    public boolean validateUser(UserLoginDTO userLoginDTO) {
        // 根据手机号查询用户信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userLoginDTO.getPhone()));
        // 判断用户是否存在且密码匹配
        return user != null && passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword());
    }

    public User findById(Integer userId) {
        // 使用MyBatis Plus提供的selectById方法来查询用户
        return userMapper.selectById(userId);
    }


    /**
     * 根据手机号加载用户详情。
     *
     * @param phone 用户的手机号，作为登录识别标识。
     * @return UserDetails 用户详情对象，包含用户的基本信息、权限等。
     * @throws UsernameNotFoundException 如果根据手机号未找到用户，抛出此异常。
     */
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        // 通过手机号查询用户信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));

        // 如果未找到对应用户，抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("User not found with phone: " + phone);
        }

        // 构造并返回Spring Security的UserDetails对象
        return new org.springframework.security.core.userdetails.User(user.getPhone(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
    }

    public User findByPhone(String phone) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
    }


    @Override
    public boolean recharge(String phone, BigDecimal amount) {
        // 查找用户
        User user = findByPhone(phone);
        if (user == null) {
            return false;
        }
        // 更新余额
        user.setBalance(user.getBalance().add(amount));
        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public boolean placeOrder(String phone, BigDecimal orderAmount) {
        // 查找用户
        User user = findByPhone(phone);
        if (user == null) {
            return false;
        }
        // 检查余额是否足够
        if (user.getBalance().compareTo(orderAmount) < 0) {
            return false; // 余额不足
        }
        // 扣除金额
        user.setBalance(user.getBalance().subtract(orderAmount));
        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public boolean resetPassword(String phone, String newPassword) {
        User user = findByPhone(phone);
        if (user != null) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            return updateUser(user); // 实现更新用户信息的逻辑
        }
        return false;
    }
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }
}
