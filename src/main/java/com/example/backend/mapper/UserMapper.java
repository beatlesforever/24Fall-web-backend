package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */
public interface UserMapper extends BaseMapper<User> {

}
