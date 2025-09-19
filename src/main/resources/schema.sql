-- 文献助手数据库DDL
CREATE DATABASE IF NOT EXISTS literature_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE literature_db;

-- 文献表
CREATE TABLE IF NOT EXISTS literature (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '文献标题',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    original_file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(10) NOT NULL COMMENT '文件类型',
    tags VARCHAR(500) COMMENT '标签，多个用逗号分隔',
    description TEXT COMMENT '文献描述',
    reading_guide LONGTEXT COMMENT '阅读指南',
    category VARCHAR(100) COMMENT '分类',
    author VARCHAR(200) COMMENT '作者',
    publish_year INT COMMENT '出版年份',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
    INDEX idx_category (category),
    INDEX idx_tags (tags(255)),
    INDEX idx_create_time (create_time),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文献表';

-- 插入示例数据
INSERT INTO literature (title, file_path, original_file_name, file_size, file_type, tags, description, category, author, publish_year) VALUES
('人工智能研究综述', '/uploads/ai_research.pdf', 'ai_research.pdf', 1048576, '.pdf', '人工智能,机器学习,综述', '关于人工智能领域最新研究的综述文章', '计算机科学', '张三', 2023),
('深度学习在医疗中的应用', '/uploads/dl_healthcare.pdf', 'dl_healthcare.pdf', 2097152, '.pdf', '深度学习,医疗,AI应用', '探讨深度学习技术在医疗诊断中的应用', '人工智能', '李四', 2024);