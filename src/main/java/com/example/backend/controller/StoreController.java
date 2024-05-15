package com.example.backend.controller;

import com.example.backend.entity.Store;
import com.example.backend.service.IStoreService;
import com.example.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhaoran
 * @date 2024/5/15
 * @project Backend
 */
@RestController
@RequestMapping("/api/store")
public class StoreController {

    @Autowired
    IStoreService storeService;

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 创建商店。
     *
     * @param store 包含商店信息的请求体，通过RequestBody接收
     * @return 返回一个响应实体，包含创建成功后的商店信息和HTTP状态码
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStore(@RequestBody Store store) {
        if (store == null || store.getName() == null || store.getLocation() == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的商店参数", null);
        }

        storeService.save(store);
        return createResponse(HttpStatus.CREATED, "商店创建成功", store);
    }

    /**
     * 更新商店信息。
     *
     * @param storeId 商店ID，通过路径变量传递。
     * @param store 更新后的商店对象，通过RequestBody接收前端传来的数据。
     * @return 返回响应实体，包含更新后的商店对象和状态码200 OK。
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<Map<String, Object>> updateStore(@PathVariable Integer storeId, @RequestBody Store store) {
        if (store == null || store.getName() == null || store.getLocation() == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的商店参数", null);
        }

        Store existingStore = storeService.getById(storeId);
        if (existingStore == null) {
            return createResponse(HttpStatus.NOT_FOUND, "商店未找到", null);
        }

        store.setStoreId(storeId);
        storeService.updateById(store);

        Store updatedStore = storeService.getById(storeId);
        return createResponse(HttpStatus.OK, "商店更新成功", updatedStore);
    }

    /**
     * 删除指定的商店。
     *
     * @param storeId 商店ID，通过路径变量传递。
     * @return 返回响应实体，包含状态码200 OK表示删除成功，404 Not Found表示商店未找到。
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Map<String, Object>> deleteStore(@PathVariable Integer storeId) {
        if (storeId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的商店ID", null);
        }

        boolean removed = storeService.removeById(storeId);
        if (removed) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("storeId", storeId);
            return createResponse(HttpStatus.OK, "商店删除成功", responseData);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "商店未找到", null);
        }
    }

    /**
     * 根据商店ID获取商店详情。
     *
     * @param storeId 商店ID，通过路径变量传递。
     * @return 返回响应实体，包含商店对象和状态码200 OK，或状态码404 Not Found表示未找到。
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<Map<String, Object>> getStoreById(@PathVariable Integer storeId) {
        if (storeId == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "无效的商店ID", null);
        }

        Store store = storeService.getById(storeId);
        if (store != null) {
            return createResponse(HttpStatus.OK, "查询成功", store);
        } else {
            return createResponse(HttpStatus.NOT_FOUND, "商店未找到", null);
        }
    }

    /**
     * 获取所有商店。
     *
     * @return 返回响应实体，包含所有商店列表和状态码200 OK。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStores() {
        List<Store> stores = storeService.list();

        if (stores.isEmpty()) {
            return createResponse(HttpStatus.NOT_FOUND, "没有找到任何商店", null);
        }

        return createResponse(HttpStatus.OK, "查询成功", stores);
    }
}
