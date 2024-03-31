package com.example.backend.controller;

import com.example.backend.service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@Controller
public class ReviewController {
    @Autowired
    IReviewService reviewService;

}
