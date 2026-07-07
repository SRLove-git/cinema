-- 创建数据库
CREATE DATABASE IF NOT EXISTS cinema_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cinema_db;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'staff' COMMENT 'admin-管理员, staff-普通员工, customer-顾客',
    `real_name` VARCHAR(50) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 电影表
DROP TABLE IF EXISTS `movie`;
CREATE TABLE `movie` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '电影名称',
    `genre` VARCHAR(100) DEFAULT NULL COMMENT '类型/ genre',
    `duration` INT NOT NULL COMMENT '时长（分钟）',
    `language` VARCHAR(50) DEFAULT NULL COMMENT '语言',
    `release_date` DATE DEFAULT NULL COMMENT '上映日期',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `poster` VARCHAR(500) DEFAULT NULL COMMENT '海报路径',
    `status` TINYINT DEFAULT 1 COMMENT '0-下架, 1-在映',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 影厅表
DROP TABLE IF EXISTS `hall`;
CREATE TABLE `hall` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '影厅名称',
    `rows` INT NOT NULL COMMENT '行数',
    `cols` INT NOT NULL COMMENT '列数',
    `description` VARCHAR(200) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 放映计划表
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `movie_id` INT NOT NULL,
    `hall_id` INT NOT NULL,
    `show_date` DATE NOT NULL COMMENT '放映日期',
    `show_time` TIME NOT NULL COMMENT '放映时间',
    `price` DECIMAL(10,2) NOT NULL COMMENT '票价',
    `status` TINYINT DEFAULT 1 COMMENT '0-取消, 1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`movie_id`) REFERENCES `movie`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`hall_id`) REFERENCES `hall`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 座位表（每个影厅、每个场次的座位状态）
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `schedule_id` INT NOT NULL COMMENT '场次ID',
    `row_num` INT NOT NULL COMMENT '行号',
    `col_num` INT NOT NULL COMMENT '列号',
    `status` TINYINT DEFAULT 0 COMMENT '0-可选, 1-已售, 2-锁定',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`schedule_id`) REFERENCES `schedule`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` INT NOT NULL,
    `schedule_id` INT NOT NULL,
    `seat_ids` VARCHAR(200) NOT NULL COMMENT '座位ID列表，逗号分隔',
    `quantity` INT NOT NULL COMMENT '数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `status` VARCHAR(20) DEFAULT 'paid' COMMENT 'paid-已支付, refunded-已退款',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`schedule_id`) REFERENCES `schedule`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员和测试用户
INSERT INTO `user` (`username`, `password`, `role`, `real_name`) VALUES
('admin', 'admin123', 'admin', '系统管理员'),
('staff01', 'staff123', 'staff', '张三'),
('customer01', 'customer123', 'customer', '李四');

-- 插入测试电影数据
INSERT INTO `movie` (`title`, `genre`, `duration`, `language`, `release_date`, `description`) VALUES
('流浪地球3', '科幻', 142, '中文', '2026-01-29', '太阳即将毁灭，人类在地球表面建造出巨大的推进器，寻找新的家园。'),
('哪吒之魔童闹海', '动画', 110, '中文', '2026-02-10', '哪吒重生后，与东海龙族展开新的较量。'),
('唐探1900', '喜剧/悬疑', 136, '中文', '2026-01-29', '唐仁和秦风穿越到1900年的美国旧金山，破解神秘案件。'),
('蛟龙行动', '动作/战争', 128, '中文', '2026-01-29', '中国海军蛟龙突击队执行海外撤侨任务的英勇故事。'),
('熊出没·重启未来', '动画', 98, '中文', '2026-01-29', '光头强和熊大熊二意外穿越到未来世界，展开新的冒险。');

-- 插入测试影厅数据
INSERT INTO `hall` (`name`, `rows`, `cols`, `description`) VALUES
('1号厅（IMAX）', 8, 12, 'IMAX巨幕厅，支持3D'),
('2号厅（VIP）', 6, 8, 'VIP豪华厅，真皮座椅'),
('3号厅（标准）', 10, 10, '标准数字厅'),
('4号厅（杜比）', 8, 10, '杜比全景声厅');
