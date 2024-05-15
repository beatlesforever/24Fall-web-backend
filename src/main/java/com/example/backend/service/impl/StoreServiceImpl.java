package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Store;
import com.example.backend.entity.UserCoupon;
import com.example.backend.mapper.StoreMapeer;
import com.example.backend.mapper.UserCouponMapper;
import com.example.backend.service.IStoreService;
import com.example.backend.service.IUserCouponService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/5/15
 * @project Backend
 */

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapeer, Store> implements IStoreService {
}
