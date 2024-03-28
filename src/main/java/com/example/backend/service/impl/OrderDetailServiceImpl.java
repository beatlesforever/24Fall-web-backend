package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.OrderDetail;
import com.example.backend.mapper.OrderDetailMapper;
import com.example.backend.service.IOrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements IOrderDetailService {
}
