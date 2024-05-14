
-- 1. 用户表（users）
CREATE TABLE users (
                       user_id INT PRIMARY KEY AUTO_INCREMENT, -- 用户ID，唯一标识
                       name VARCHAR(255) NOT NULL, -- 用户姓名
                       phone VARCHAR(20) UNIQUE NOT NULL, -- 手机号，用于登录和联系
                       password VARCHAR(255) NOT NULL, -- 密码，加密存储
                       registration_date DATE NOT NULL, -- 注册日期
                       balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- 预存款金额
                       role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' -- 用户角色，例如ADMIN或CUSTOMER
);

-- 2. 店铺表（stores）
CREATE TABLE stores (
                        store_id INT PRIMARY KEY AUTO_INCREMENT, -- 店铺ID，唯一标识
                        name VARCHAR(255) NOT NULL, -- 店铺名称
                        location VARCHAR(255) NOT NULL -- 店铺位置
);

-- 3. 菜单表（menu_items）
CREATE TABLE menu_items (
                            item_id INT PRIMARY KEY AUTO_INCREMENT, -- 菜品ID，唯一标识
                            store_id INT NOT NULL, -- 店铺ID，外键
                            name VARCHAR(255) NOT NULL, -- 菜品名称
                            description TEXT, -- 介绍
                            image_url VARCHAR(255), -- 图片URL
                            category VARCHAR(50) NOT NULL, -- 类别
                            small_size_price DECIMAL(10, 2) NOT NULL, -- 小份价格
                            large_size_price DECIMAL(10, 2) NOT NULL, -- 大份价格
                            size_stock INT NOT NULL DEFAULT 0, -- 库存
                            FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

-- 4. 订单表（orders）
CREATE TABLE orders (
                        order_id INT PRIMARY KEY AUTO_INCREMENT, -- 订单ID，唯一标识
                        user_id INT NOT NULL, -- 用户ID，外键
                        store_id INT NOT NULL, -- 店铺ID，外键
                        status VARCHAR(50) NOT NULL, -- 订单状态(已创建、已确认、进行中、已完成、已取消)
                        total_price DECIMAL(10, 2) NOT NULL, -- 总价
                        order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 下单时间
                        notes TEXT, -- 备注
                        dine_option VARCHAR(20) NOT NULL, -- 就餐选项（堂食/自提）
                        FOREIGN KEY (user_id) REFERENCES users(user_id),
                        FOREIGN KEY (store_id) REFERENCES stores(store_id)
);

-- 5. 订单详情表（order_details）
CREATE TABLE order_details (
                               detail_id INT PRIMARY KEY AUTO_INCREMENT, -- 订单详情ID，唯一标识
                               order_id INT NOT NULL, -- 订单ID，外键
                               item_id INT NOT NULL, -- 菜品ID，外键
                               quantity INT NOT NULL, -- 数量
                               size VARCHAR(50) NOT NULL, -- 规格（小份/大份）
                               price DECIMAL(10, 2) NOT NULL, -- 单价
                               special_requests TEXT, -- 特殊需求备注
                               FOREIGN KEY (order_id) REFERENCES orders(order_id),
                               FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- 6. 评价表（reviews）
CREATE TABLE reviews (
                         review_id INT PRIMARY KEY AUTO_INCREMENT, -- 评价ID，唯一标识
                         user_id INT NOT NULL, -- 用户ID，外键
                         item_id INT NOT NULL, -- 菜品ID，外键
                         rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5), -- 评分，1到5之间
                         comment TEXT, -- 评论
                         review_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 评价时间
                         FOREIGN KEY (user_id) REFERENCES users(user_id),
                         FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- 7. 优惠券表（coupons）
CREATE TABLE coupons (
                         coupon_id INT PRIMARY KEY AUTO_INCREMENT, -- 优惠券ID，唯一标识
                         code VARCHAR(50) UNIQUE NOT NULL, -- 优惠码
                         discount DECIMAL(5, 2) NOT NULL, -- 折扣金额或百分比
                         expiration_date DATE NOT NULL, -- 过期日期
                         min_purchase DECIMAL(10, 2) DEFAULT 0.00, -- 最低消费金额
                         is_active BOOLEAN NOT NULL DEFAULT TRUE -- 优惠券是否有效
);

-- 8. 用户优惠券表（user_coupons）
CREATE TABLE user_coupons (
                              user_coupon_id INT PRIMARY KEY AUTO_INCREMENT, -- 用户优惠券ID，唯一标识
                              user_id INT NOT NULL, -- 用户ID，外键
                              coupon_id INT NOT NULL, -- 优惠券ID，外键
                              is_used BOOLEAN NOT NULL DEFAULT FALSE, -- 是否已使用
                              FOREIGN KEY (user_id) REFERENCES users(user_id),
                              FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id)
);