package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Review;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.service.IReviewService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements IReviewService {
}
