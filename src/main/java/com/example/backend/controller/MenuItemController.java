package com.example.backend.controller;

import com.example.backend.entity.MenuItem;
import com.example.backend.service.IMenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.Authentication;

import static com.example.backend.entity.Roles.ADMIN;

/**
 * @author zhouhaoran
 * @date 2024/3/29
 * @project Backend
 */
@RestController
@RequestMapping("/api/menu/items")
public class MenuItemController {
    @Autowired
    IMenuItemService menuItemService;

    /**
     * 获取所有菜单项的信息。
     *
     * @param authentication 用户的认证信息，用于验证用户身份。
     * @return 返回一个响应实体，包含所有菜单项的列表。如果用户未认证，返回401状态码。
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems(Authentication authentication) {
        // 验证用户认证信息是否合法
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // 用户未认证，返回401状态码
        }

        List<MenuItem> items = menuItemService.list(); // 从服务层获取所有菜单项
        return ResponseEntity.ok(items); // 返回200状态码和菜单项列表
    }

    /**
     * 根据商品ID获取菜单项信息。
     *
     * @param itemId 要获取的菜单项的ID，路径变量。
     * @param authentication 当前请求的认证信息，用于权限验证。
     * @return 如果找到了对应的菜单项，则返回包含该菜单项信息的ResponseEntity；如果没有找到，则返回一个表示资源不存在的ResponseEntity。
     *         如果用户未认证，返回401状态码。
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<MenuItem> getMenuItem(@PathVariable Long itemId, Authentication authentication) {
        // 验证用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        MenuItem item = menuItemService.getById(itemId); // 从服务中尝试获取指定ID的菜单项
        // 根据菜单项是否存在，返回不同的ResponseEntity
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }


    /**
     * 通过POST请求更新一个现有的菜单项。
     * 该方法需要用户具有ADMIN权限才能访问。
     *
     * @param itemId 菜单项的ID，通过URL路径变量传入，用于指定要更新的菜单项。
     * @param menuItem 通过请求体传入的菜单项对象，包含了更新菜单项所需的详细信息。
     * @param authentication 当前用户的认证信息，用于权限检查。
     * @return 返回一个响应实体，表示菜单项更新是否成功。
     */
    @Secured(ADMIN)
    @PutMapping("/{itemId}")
    public ResponseEntity<String> updateMenuItem(@PathVariable Integer itemId, @RequestBody MenuItem menuItem, Authentication authentication) {
        // 检查用户是否已认证，未认证返回401状态码
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // 设置菜单项ID，准备更新
        menuItem.setItemId(itemId);
        menuItemService.updateById(menuItem); // 通过服务层更新菜单项信息
        return ResponseEntity.ok("菜单项更新成功"); // 返回成功响应
    }


    /**
     * 通过POST请求添加新的菜单项。
     * 该操作需要用户认证，仅管理员角色有权限执行。
     *
     * @param menuItem 通过RequestBody接收的菜单项对象，包含菜单项的详细信息。
     * @param authentication 用户的认证信息，用于权限检查。
     * @return 如果用户未认证，返回401状态码；添加成功则返回200状态码和成功消息。
     */
    @Secured(ADMIN)
    @PostMapping
    public ResponseEntity<String> addMenuItem(@RequestBody MenuItem menuItem, Authentication authentication) {
        // 检查用户是否已认证，未认证返回401
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // 保存菜单项
        menuItemService.save(menuItem);
        return ResponseEntity.ok("菜单项添加成功");
    }

    /**
     * 删除菜单项
     *
     * @param itemId 要删除的菜单项的ID，通过URL路径变量传递。
     * @param authentication 当前用户的认证信息，用于权限检查。
     * @return 如果删除成功，返回状态码200和删除成功的消息；如果删除失败（如项目不存在），返回状态码404。
     */
    @Secured(ADMIN) // 限定只有拥有ADMIN权限的角色才能访问该方法
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteMenuItem(@PathVariable Long itemId, Authentication authentication) {
        // 检查用户是否已认证，未认证返回401状态码
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // 尝试根据ID删除菜单项，成功或失败根据返回值处理
        boolean success = menuItemService.removeById(itemId);
        return success ? ResponseEntity.ok("菜单项删除成功") : ResponseEntity.notFound().build();
    }
}
