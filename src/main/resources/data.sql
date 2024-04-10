-- 插入用户数据时包括角色
INSERT INTO users (name, phone, password, registration_date, balance, role) VALUES
                                                                                ('张三', '13800000000', 'password1', '2024-03-29', 100.00, 'ROLE_CUSTOMER'),
                                                                                ('李四', '13911111111', 'password2', '2024-03-28', 150.50, 'ROLE_CUSTOMER'),
                                                                                ('王五', '13722222222', 'password3', '2024-03-27', 200.75, 'ROLE_CUSTOMER'),
                                                                                ('管理员', '18888888888', 'adminpass', '2024-03-26', 300.00, 'ROLE_ADMIN'); -- 假设你也想插入一个管理员账号
-- 插入菜单项数据
INSERT INTO menu_items (name, price, description, image_url, category) VALUES
                                                                           ('宫保鸡丁', 25.00, '经典川菜，麻辣鲜香', 'image1.jpg', '川菜'),
                                                                           ('清蒸鲈鱼', 38.00, '鱼肉细嫩，汤汁鲜美', 'image2.jpg', '粤菜'),
                                                                           ('番茄炒蛋', 15.00, '简单营养，家常味道', 'image3.jpg', '家常菜');

-- 插入订单数据
INSERT INTO orders (user_id, status, total_price, order_time, notes) VALUES
                                                                         (1, '已完成', 63.00, '2024-03-29 12:00:00', '多放辣'),
                                                                         (2, '制作中', 15.00, '2024-03-29 12:30:00', NULL),
                                                                         (3, '待支付', 25.00, '2024-03-29 13:00:00', '不要葱');

-- 插入订单详情数据
INSERT INTO order_details (order_id, item_id, quantity, price, size) VALUES
                                                                         (1, 1, 1, 25.00, NULL),
                                                                         (1, 2, 1, 38.00, '大份'),
                                                                         (2, 3, 1, 15.00, NULL),
                                                                         (3, 1, 1, 25.00, '小份');