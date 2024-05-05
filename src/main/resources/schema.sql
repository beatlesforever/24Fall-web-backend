
-- 1. 用户表（users）
CREATE TABLE users (
                       user_id INT PRIMARY KEY AUTO_INCREMENT, -- 用户ID，唯一标识
                       name VARCHAR(255) NOT NULL, -- 用户姓名
                       phone VARCHAR(20) UNIQUE NOT NULL, -- 手机号，用于登录和联系
                       password VARCHAR(255) NOT NULL, -- 密码，加密存储
                       registration_date DATE NOT NULL, -- 注册日期
                       balance DECIMAL(10, 2) NOT NULL, -- 预存款金额
                       role VARCHAR(20) NOT NULL                -- 用户角色，例如ADMIN或CUSTOMER

);

-- 2. 菜单表（menu_items）
CREATE TABLE menu_items (
                            item_id INT PRIMARY KEY AUTO_INCREMENT, -- 菜品ID，唯一标识
                            name VARCHAR(255) NOT NULL, -- 菜品名称
                            price DECIMAL(10, 2) NOT NULL, -- 价格
                            description TEXT, -- 介绍
                            image_url VARCHAR(255), -- 图片URL
                            category VARCHAR(50) NOT NULL -- 类别
);

-- 3. 订单表（orders）
CREATE TABLE orders (
                        order_id INT PRIMARY KEY AUTO_INCREMENT, -- 订单ID，唯一标识
                        user_id INT NOT NULL, -- 用户ID，外键
                        status VARCHAR(50) NOT NULL, -- 订单状态(已下单、已完成)
                        total_price DECIMAL(10, 2) NOT NULL, -- 总价
                        order_time TIMESTAMP NOT NULL, -- 下单时间
                        notes TEXT, -- 备注
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 4. 订单详情表（order_details）
CREATE TABLE order_details (
                               detail_id INT PRIMARY KEY AUTO_INCREMENT, -- 订单详情ID，唯一标识
                               order_id INT NOT NULL, -- 订单ID，外键
                               item_id INT NOT NULL, -- 菜品ID，外键
                               quantity INT NOT NULL, -- 数量
                               price DECIMAL(10, 2) NOT NULL, -- 单价
                               size VARCHAR(50), -- 规格
                               FOREIGN KEY (order_id) REFERENCES orders(order_id),
                               FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- 5. 评价表（reviews）
CREATE TABLE reviews (
                     review_id INT PRIMARY KEY AUTO_INCREMENT, -- 评价ID，唯一标识
                     user_id INT NOT NULL, -- 用户ID，外键
                     item_id INT NOT NULL, -- 菜品ID，外键
                     rating INT NOT NULL, -- 评分
                     comment TEXT, -- 评论
                     review_time TIMESTAMP NOT NULL, -- 评价时间
                     FOREIGN KEY (user_id) REFERENCES users(user_id),
                     FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);