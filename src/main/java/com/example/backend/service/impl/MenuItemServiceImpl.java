package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.MenuItem;
import com.example.backend.mapper.MenuItemMapper;
import com.example.backend.service.IMenuItemService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/3/28
 * @project Backend
 */
@Service
public class MenuItemServiceImpl extends ServiceImpl<MenuItemMapper, MenuItem> implements IMenuItemService {
}
