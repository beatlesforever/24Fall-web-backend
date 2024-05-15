package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Coupon;
import com.example.backend.entity.MenuItem;
import com.example.backend.mapper.CouponMapper;
import com.example.backend.mapper.MenuItemMapper;
import com.example.backend.service.ICouponService;
import com.example.backend.service.IMenuItemService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/5/15
 * @project Backend
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {
}
