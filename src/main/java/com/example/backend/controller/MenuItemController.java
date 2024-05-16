package com.example.backend.controller;

import com.example.backend.entity.MenuItem;
import com.example.backend.service.IMenuItemService;
import com.example.backend.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.backend.entity.OrderDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    IOrderDetailService orderDetailService;  // 注入订单详情服务

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 获取所有菜单项的信息。
     *
     * @param authentication 用户的认证信息，用于验证用户身份。
     * @return 返回一个响应实体，包含所有菜单项的列表。如果用户未认证，返回401状态码。
     */
    @GetMapping
    public ResponseEntity<?> getAllMenuItems(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        List<MenuItem> items = menuItemService.list();
        return createResponse(HttpStatus.OK, "获取所有菜单项成功", items);
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
    public ResponseEntity<?> getMenuItem(@PathVariable Long itemId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }
        MenuItem item = menuItemService.getById(itemId);
        return item != null ? createResponse(HttpStatus.OK, "获取菜单项成功", item) : createResponse(HttpStatus.NOT_FOUND, "菜单项不存在", null);
    }

    /**
     * 获取某店铺的所有菜单项。
     *
     * @param storeId 店铺ID，通过路径变量传递。
     * @param authentication 用户的认证信息，用于验证用户身份。
     * @return 返回一个响应实体，包含该店铺所有菜单项的列表。如果用户未认证，返回401状态码。
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getMenuItemsByStore(@PathVariable Integer storeId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 查询某店铺的所有菜单项
        List<MenuItem> items = menuItemService.lambdaQuery().eq(MenuItem::getStoreId, storeId).list();
        return createResponse(HttpStatus.OK, "获取店铺菜单项成功", items);
    }

    /**
     * 通过PUT请求更新一个现有的菜单项。
     * 该方法需要用户具有ADMIN权限才能访问。
     *
     * @param itemId 菜单项的ID，通过URL路径变量传入，用于指定要更新的菜单项。
     * @param menuItem 通过请求体传入的菜单项对象，包含了更新菜单项所需的详细信息。
     * @param authentication 当前用户的认证信息，用于权限检查。
     * @return 返回一个响应实体，表示菜单项更新是否成功。
     */
    @Secured(ADMIN)
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Integer itemId, @RequestBody MenuItem menuItem, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 尝试获取菜单项，如果不存在，则返回404
        MenuItem item = menuItemService.getById(itemId);
        if (item == null) {
            return createResponse(HttpStatus.NOT_FOUND, "菜单项不存在，无法更新", null);
        }

        menuItem.setItemId(itemId);
        menuItemService.updateById(menuItem);
        MenuItem updatedItem = menuItemService.getById(itemId); // 获取更新后的菜单项
        return createResponse(HttpStatus.OK, "菜单项更新成功", updatedItem);
    }

    /**
     * 搜索菜单项信息。
     *
     * @param query 搜索关键词，通过URL查询参数传递。
     * @param authentication 当前请求的认证信息，用于权限验证。
     * @return 返回包含搜索结果的ResponseEntity，如果没有找到，则返回空列表。
     *         如果用户未认证，返回401状态码。
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMenuItems(@RequestParam String query, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 使用MyBatis-Plus的lambda查询构建器进行模糊查询
        List<MenuItem> items = menuItemService.lambdaQuery()
                .like(MenuItem::getName, query)
                .or()
                .like(MenuItem::getDescription, query)
                .or()
                .like(MenuItem::getCategory, query)
                .list();

        return createResponse(HttpStatus.OK, "搜索菜单项成功", items);
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
    public ResponseEntity<?> addMenuItem(@RequestBody MenuItem menuItem, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        menuItemService.save(menuItem);
        return createResponse(HttpStatus.OK, "菜单项添加成功", menuItem);
    }

    /**
     * 删除菜单项
     *
     * @param itemId 要删除的菜单项的ID，通过URL路径变量传递。
     * @param authentication 当前用户的认证信息，用于权限检查。
     * @return 如果删除成功，返回状态码200和删除成功的消息；如果删除失败（如项目不存在），返回状态码404。
     */
    @Secured(ADMIN)
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long itemId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户未认证", null);
        }

        // 尝试获取菜单项，如果不存在，则返回404
        MenuItem item = menuItemService.getById(itemId);
        if (item == null) {
            return createResponse(HttpStatus.NOT_FOUND, "菜单项不存在，无法删除", null);
        }

        // 删除相关订单详情，如果没有详情关联，则这一步不会有删除动作，但不应视为错误
        boolean detailsDeleted = orderDetailService.lambdaUpdate().eq(OrderDetail::getItemId, itemId).remove();

        // 删除菜单项本身
        boolean menuItemDeleted = menuItemService.removeById(itemId);
        if (!menuItemDeleted) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "删除菜单项失败", null);
        }

        // 如果一切顺利，即使没有订单详情被删除，也返回成功删除的消息
        return createResponse(HttpStatus.OK, "菜单项及其相关订单详情已成功删除", null);
    }

}
