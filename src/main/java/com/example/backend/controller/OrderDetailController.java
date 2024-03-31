package com.example.backend.controller;

import com.example.backend.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@Controller
public class OrderDetailController {
    @Autowired
    IOrderDetailService orderDetailService;
}
