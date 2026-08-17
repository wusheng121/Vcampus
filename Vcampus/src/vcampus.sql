-- ============================================================
--  Vcampus 数据库完整建表脚本
--  MySQL 8.x  |  utf8mb4
--  说明：由各 DAO 的 SQL 反推得出，覆盖全部 16 张业务表。
--  种子用户密码均为 "123456" 的 SHA-256 小写十六进制哈希：
--      8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- ============================================================

DROP DATABASE IF EXISTS vcampus;
CREATE DATABASE vcampus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vcampus;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------ 用户 / 账号
-- 注：phone、address 列被 UserService.updateUserInfo 使用（原脚本缺失，导致更新失败）
CREATE TABLE `user` (
    `user_id`    VARCHAR(20)  NOT NULL,
    `password`   VARCHAR(64)  NOT NULL,
    `role`       ENUM('student','teacher','admin') NOT NULL,
    `name`       VARCHAR(50)  DEFAULT NULL,
    `email`      VARCHAR(100) DEFAULT NULL,
    `phone`      VARCHAR(20)  DEFAULT NULL,
    `address`    VARCHAR(200) DEFAULT NULL,
    `created_at` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ 学生（学籍）
CREATE TABLE `student` (
    `student_id`   VARCHAR(20) NOT NULL,
    `student_name` VARCHAR(50) DEFAULT NULL,
    `sex`          VARCHAR(4)  DEFAULT NULL,
    `identity`     VARCHAR(18) DEFAULT NULL,
    `user_id`      VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (`student_id`),
    UNIQUE KEY `uk_student_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学生个人基本信息（1:1 student）
CREATE TABLE `student_personal` (
    `student_id` VARCHAR(20)  NOT NULL,
    `phone`      VARCHAR(20)  DEFAULT NULL,
    `email`      VARCHAR(100) DEFAULT NULL,
    `address`    VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (`student_id`),
    CONSTRAINT `fk_sp_student` FOREIGN KEY (`student_id`)
        REFERENCES `student` (`student_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学生个人资料变更审核（1:1 student，提交→审核）
CREATE TABLE `student_personal_audit` (
    `student_id`  VARCHAR(20)  NOT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `email`       VARCHAR(100) DEFAULT NULL,
    `address`     VARCHAR(200) DEFAULT NULL,
    `status`      ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    `apply_time`  DATETIME     DEFAULT NULL,
    `audit_time`  DATETIME     DEFAULT NULL,
    `auditor_id`  VARCHAR(20)  DEFAULT NULL,
    `remark`      VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`student_id`),
    CONSTRAINT `fk_spa_student` FOREIGN KEY (`student_id`)
        REFERENCES `student` (`student_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ 教师
CREATE TABLE `teacher` (
    `teacher_id`   VARCHAR(20)  NOT NULL,
    `teacher_name` VARCHAR(50)  DEFAULT NULL,
    `sex`          VARCHAR(4)   DEFAULT NULL,
    `subject`      VARCHAR(50)  DEFAULT NULL,
    `user_id`      VARCHAR(20)  DEFAULT NULL,
    `phone`        VARCHAR(20)  DEFAULT NULL,
    `email`        VARCHAR(100) DEFAULT NULL,
    `address`      VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (`teacher_id`),
    UNIQUE KEY `uk_teacher_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ 课程 / 排课 / 选课
CREATE TABLE `course` (
    `course_id`   INT NOT NULL AUTO_INCREMENT,
    `course_code` VARCHAR(20)  DEFAULT NULL,
    `course_name` VARCHAR(100) DEFAULT NULL,
    `credit`      DOUBLE       DEFAULT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`course_id`),
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lesson` (
    `lesson_id`     INT NOT NULL AUTO_INCREMENT,
    `course_id`     INT          DEFAULT NULL,
    `capacity`      INT          DEFAULT 0,
    `is_open`       TINYINT(1)   DEFAULT 0,
    `enroll_start`  DATETIME     DEFAULT NULL,
    `enroll_end`    DATETIME     DEFAULT NULL,
    `classroom`     VARCHAR(50)  DEFAULT NULL,
    `remark`        VARCHAR(255) DEFAULT NULL,
    `teacher_id`    VARCHAR(20)  DEFAULT NULL,
    PRIMARY KEY (`lesson_id`),
    CONSTRAINT `fk_lesson_course` FOREIGN KEY (`course_id`)
        REFERENCES `course` (`course_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `lesson_time` (
    `time_id`     INT NOT NULL AUTO_INCREMENT,
    `lesson_id`   INT          DEFAULT NULL,
    `day_of_week` INT          DEFAULT NULL,
    `start_sec`   INT          DEFAULT NULL,
    `end_sec`     INT          DEFAULT NULL,
    `location`    VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`time_id`),
    CONSTRAINT `fk_lt_lesson` FOREIGN KEY (`lesson_id`)
        REFERENCES `lesson` (`lesson_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enrollment` (
    `enroll_id`  INT NOT NULL AUTO_INCREMENT,
    `student_id` VARCHAR(20) DEFAULT NULL,
    `lesson_id`  INT         DEFAULT NULL,
    `status`     ENUM('enrolled','dropped') DEFAULT 'enrolled',
    `created_at` TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`enroll_id`),
    UNIQUE KEY `uk_enroll` (`student_id`, `lesson_id`),
    CONSTRAINT `fk_enr_lesson`  FOREIGN KEY (`lesson_id`)
        REFERENCES `lesson` (`lesson_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_enr_student` FOREIGN KEY (`student_id`)
        REFERENCES `student` (`student_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ 图书馆（书 + 借阅 + 期刊）
CREATE TABLE `book` (
    `book_id`   VARCHAR(20)  NOT NULL,
    `book_name` VARCHAR(100) DEFAULT NULL,
    `author`    VARCHAR(50)  DEFAULT NULL,
    `number`    INT          DEFAULT 0,
    `status`    VARCHAR(20)  DEFAULT NULL,
    `theme`     VARCHAR(50)  DEFAULT NULL,
    `borrowers` INT          DEFAULT 0,
    PRIMARY KEY (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `borrow_record` (
    `id`          INT NOT NULL AUTO_INCREMENT,
    `user_id`     VARCHAR(20)  DEFAULT NULL,
    `book_id`     VARCHAR(20)  DEFAULT NULL,
    `borrow_date` DATETIME     DEFAULT NULL,
    `due_date`    DATETIME     DEFAULT NULL,
    `return_date` DATETIME     DEFAULT NULL,
    `status`      VARCHAR(20)  DEFAULT NULL,
    `fine`        DOUBLE       DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_br_user` (`user_id`),
    KEY `idx_br_book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 注意：DAO 中写为 "Journal"（大写 J），此处保持一致以兼容大小写敏感的 MySQL。
CREATE TABLE `Journal` (
    `journal_id`   INT NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(100) DEFAULT NULL,
    `category`     VARCHAR(50)  DEFAULT NULL,
    `publish_date` DATE         DEFAULT NULL,
    `publisher`    VARCHAR(100) DEFAULT NULL,
    `description`  VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`journal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ 商店（商品 + 分类 + 订单）
CREATE TABLE `product_categories` (
    `category_id`   VARCHAR(20)  NOT NULL,
    `category_name` VARCHAR(50)  DEFAULT NULL,
    `description`   VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `product` (
    `product_id`  VARCHAR(20)  NOT NULL,
    `name`        VARCHAR(100) DEFAULT NULL,
    `price`       DECIMAL(10,2) DEFAULT NULL,
    `stock`       INT          DEFAULT 0,
    `status`      VARCHAR(20)  DEFAULT NULL,
    `category_id` VARCHAR(20)  DEFAULT NULL,
    `created_at`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`product_id`),
    KEY `idx_prod_cat` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `orders` (
    `order_id`     VARCHAR(20)   NOT NULL,
    `user_id`      VARCHAR(20)   DEFAULT NULL,
    `total_amount` DECIMAL(10,2) DEFAULT NULL,
    `status`       VARCHAR(20)   DEFAULT NULL,
    `created_at`   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`order_id`),
    KEY `idx_order_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 注意：表名为 order_items（复数），与 OrderDAOImpl 中 4 处用法一致。
-- 原 ProductDAOImpl.isProductInOrders 与 OrderDAOImpl.findOrderItemsByOrderId 误用 order_item（单数）为 bug，已在代码侧修正。
CREATE TABLE `order_items` (
    `item_id`    INT NOT NULL AUTO_INCREMENT,
    `order_id`   VARCHAR(20)   DEFAULT NULL,
    `product_id` VARCHAR(20)   DEFAULT NULL,
    `quantity`   INT           DEFAULT 0,
    `price`      DECIMAL(10,2) DEFAULT NULL,
    PRIMARY KEY (`item_id`),
    UNIQUE KEY `uk_oi` (`order_id`, `product_id`),
    CONSTRAINT `fk_oi_order` FOREIGN KEY (`order_id`)
        REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ 找回密码验证码
-- 6 位数字验证码的 SHA-256 哈希、过期时间、是否已用
CREATE TABLE `password_reset` (
    `id`          INT NOT NULL AUTO_INCREMENT,
    `user_id`     VARCHAR(20)  NOT NULL,
    `code_hash`   VARCHAR(64)  NOT NULL,
    `expire_time` DATETIME     NOT NULL,
    `used`        TINYINT(1)   DEFAULT 0,
    `created_at`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_pr_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------ RBAC 权限模型
-- 权限点目录（细粒度，如 course:create）
CREATE TABLE `permission` (
    `code`   VARCHAR(64)  NOT NULL,
    `name`   VARCHAR(100) DEFAULT NULL,
    `module` VARCHAR(32)  DEFAULT NULL,
    PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色-权限映射（角色复用 user.role 枚举的三个值）
CREATE TABLE `role_permission` (
    `role`      ENUM('student','teacher','admin') NOT NULL,
    `perm_code` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`role`, `perm_code`),
    CONSTRAINT `fk_rp_perm` FOREIGN KEY (`perm_code`)
        REFERENCES `permission` (`code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
--  种子数据
--  默认账号密码均为：123456
-- ============================================================
INSERT INTO `user` (`user_id`, `password`, `role`, `name`, `email`) VALUES
  ('admin1',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'admin',   '系统管理员', 'admin1@vcampus.com'),
  ('teacher1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'teacher', '张老师',     'teacher1@vcampus.com'),
  ('teacher2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'teacher', '李老师',     'teacher2@vcampus.com'),
  ('student1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student', '王同学',     'student1@stu.vcampus.com'),
  ('student2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student', '赵同学',     'student2@stu.vcampus.com');

INSERT INTO `teacher` (`teacher_id`, `teacher_name`, `sex`, `subject`, `user_id`, `phone`, `email`, `address`) VALUES
  ('teacher1', '张老师', '男', '计算机科学与技术', 'teacher1', '13800000001', 'teacher1@vcampus.com', '南京市江北区'),
  ('teacher2', '李老师', '女', '软件工程',         'teacher2', '13800000002', 'teacher2@vcampus.com', '南京市玄武区');

INSERT INTO `student` (`student_id`, `student_name`, `sex`, `identity`, `user_id`) VALUES
  ('student1', '王同学', '男', '320000200001011234', 'student1'),
  ('student2', '赵同学', '女', '320000200102022345', 'student2');

INSERT INTO `student_personal` (`student_id`, `phone`, `email`, `address`) VALUES
  ('student1', '13900000001', 'student1@stu.vcampus.com', '南京市玄武区四牌楼2号'),
  ('student2', '13900000002', 'student2@stu.vcampus.com', '南京市江北区泰山街道');

-- 课程与排课
INSERT INTO `course` (`course_code`, `course_name`, `credit`, `description`) VALUES
  ('CS101', '数据结构',        3.0, '基础数据结构与算法'),
  ('CS102', '操作系统',        4.0, '操作系统原理与设计'),
  ('CS103', '计算机网络',      3.0, 'TCP/IP 与网络编程');

INSERT INTO `lesson` (`course_id`, `capacity`, `is_open`, `enroll_start`, `enroll_end`, `classroom`, `remark`, `teacher_id`) VALUES
  (1, 60, 1, DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY),  '教一101', '数据结构-张老师班', 'teacher1'),
  (2, 50, 1, DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY),  '教二201', '操作系统-李老师班', 'teacher2'),
  (3, 40, 0, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), '教三301', '已截止',           'teacher1');

-- lesson_time: day_of_week 1=周一…5=周五；start_sec/end_sec 为当天第 N 节课的节次
INSERT INTO `lesson_time` (`lesson_id`, `day_of_week`, `start_sec`, `end_sec`, `location`) VALUES
  (1, 1, 1, 2, '教一101'),
  (1, 3, 1, 2, '教一101'),
  (2, 2, 3, 4, '教二201'),
  (2, 4, 3, 4, '教二201'),
  (3, 5, 1, 2, '教三301');

-- 图书
INSERT INTO `book` (`book_id`, `book_name`, `author`, `number`, `status`, `theme`, `borrowers`) VALUES
  ('B001', '算法导论',       'Cormen',     5, '可借', '计算机', 0),
  ('B002', '深入理解计算机系统', 'Bryant',     3, '可借', '计算机', 1),
  ('B003', '现代操作系统',   'Tanenbaum',  2, '可借', '计算机', 0),
  ('B004', '红楼梦',         '曹雪芹',     4, '可借', '文学',   0),
  ('B005', '高等数学',       '同济大学',   6, '可借', '数学',   0);

-- 期刊
INSERT INTO `Journal` (`name`, `category`, `publish_date`, `publisher`, `description`) VALUES
  ('计算机学报',     '计算机', '2024-03-01', '中国计算机学会', 'CCF A 类中文期刊'),
  ('软件学报',       '软件',   '2024-04-01', '中国科学院',     'CCF A 类中文期刊'),
  ('Nature',         '综合',   '2024-05-01', 'Springer',       '国际顶级综合性期刊');

-- 商品分类与商品
INSERT INTO `product_categories` (`category_id`, `category_name`, `description`) VALUES
  ('C01', '文具',   '学习用品'),
  ('C02', '电子',   '电子产品'),
  ('C03', '生活',   '生活用品');

INSERT INTO `product` (`product_id`, `name`, `price`, `stock`, `status`, `category_id`) VALUES
  ('P001', '中性笔(黑)', 2.00,  200, '上架', 'C01'),
  ('P002', '笔记本A5',  8.50,  100, '上架', 'C01'),
  ('P003', 'U盘 64G',   59.90, 50,  '上架', 'C02'),
  ('P004', '计算器',     25.00, 30,  '上架', 'C02'),
  ('P005', '保温杯',     45.00, 40,  '上架', 'C03'),
  ('P006', '台灯',       88.00, 0,   '下架', 'C03');

-- 示例订单
INSERT INTO `orders` (`order_id`, `user_id`, `total_amount`, `status`) VALUES
  ('O0001', 'student1', 67.90, '已支付'),
  ('O0002', 'student2', 25.00, '待支付');

INSERT INTO `order_items` (`order_id`, `product_id`, `quantity`, `price`) VALUES
  ('O0001', 'P002', 1, 8.50),
  ('O0001', 'P003', 1, 59.40),
  ('O0002', 'P004', 1, 25.00);

-- 示例选课记录
INSERT INTO `enrollment` (`student_id`, `lesson_id`, `status`) VALUES
  ('student1', 1, 'enrolled'),
  ('student2', 1, 'enrolled');

-- 示例借阅记录
INSERT INTO `borrow_record` (`user_id`, `book_id`, `borrow_date`, `due_date`, `return_date`, `status`, `fine`) VALUES
  ('student1', 'B002', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), NULL, '借阅中', 0);

-- ============================================================
--  RBAC 种子：权限目录 + 角色-权限映射
-- ============================================================
INSERT INTO `permission` (`code`, `name`, `module`) VALUES
  ('user:create','创建用户','user'), ('user:update','更新用户','user'),
  ('user:delete','删除用户','user'), ('user:read-all','查看全部用户','user'),
  ('student:create','新增学生','student'), ('student:update','更新学生','student'),
  ('student:delete','删除学生','student'), ('student:read','查看学生','student'), ('student:audit','学籍审核','student'),
  ('course:create','创建课程','course'), ('course:update','更新课程','course'),
  ('course:delete','删除课程','course'), ('course:read','查看课程','course'),
  ('lesson:create','创建教学班','lesson'), ('lesson:update','更新教学班','lesson'),
  ('lesson:delete','删除教学班','lesson'), ('lesson:read','查看教学班','lesson'),
  ('lessontime:create','创建上课时间','lessontime'), ('lessontime:update','更新上课时间','lessontime'), ('lessontime:delete','删除上课时间','lessontime'),
  ('enrollment:enroll','选课','enrollment'), ('enrollment:drop','退课','enrollment'),
  ('enrollment:read-self','查看本人选课','enrollment'), ('enrollment:read-by-lesson','按班查名单','enrollment'),
  ('book:create','新增图书','book'), ('book:update','更新图书','book'),
  ('book:delete','删除图书','book'), ('book:read','查看图书','book'),
  ('book:borrow','借书','book'), ('book:return','还书','book'),
  ('borrowrecord:read-self','查看本人借阅','borrowrecord'), ('borrowrecord:read-all','查看全部借阅','borrowrecord'),
  ('journal:create','新增期刊','journal'), ('journal:update','更新期刊','journal'),
  ('journal:delete','删除期刊','journal'), ('journal:read','查看期刊','journal'),
  ('product:create','新增商品','product'), ('product:update','更新商品','product'),
  ('product:delete','删除商品','product'), ('product:read','查看商品','product'),
  ('category:create','新增分类','category'), ('category:delete','删除分类','category'),
  ('order:create','下单','order'), ('order:read-self','查看本人订单','order'),
  ('order:read-all','查看全部订单','order'), ('order:pay','支付订单','order'),
  ('order:cancel','取消订单','order'), ('order:update-status','更新订单状态','order'), ('order:delete','删除订单','order'),
  ('teacher:read','查看教师','teacher');

-- admin 拥有全部权限
INSERT INTO `role_permission` (`role`, `perm_code`)
SELECT 'admin', `code` FROM `permission`;

-- teacher 权限
INSERT INTO `role_permission` (`role`, `perm_code`) VALUES
  ('teacher','course:read'), ('teacher','lesson:read'), ('teacher','enrollment:read-by-lesson'),
  ('teacher','teacher:read'), ('teacher','student:read'),
  ('teacher','book:read'), ('teacher','book:borrow'), ('teacher','book:return'),
  ('teacher','borrowrecord:read-self'), ('teacher','journal:read'),
  ('teacher','product:read'), ('teacher','order:create'), ('teacher','order:read-self'),
  ('teacher','order:pay'), ('teacher','order:cancel');

-- student 权限
INSERT INTO `role_permission` (`role`, `perm_code`) VALUES
  ('student','course:read'), ('student','lesson:read'),
  ('student','enrollment:enroll'), ('student','enrollment:drop'), ('student','enrollment:read-self'),
  ('student','book:read'), ('student','book:borrow'), ('student','book:return'),
  ('student','borrowrecord:read-self'), ('student','journal:read'),
  ('student','product:read'), ('student','order:create'), ('student','order:read-self'),
  ('student','order:pay'), ('student','order:cancel');
