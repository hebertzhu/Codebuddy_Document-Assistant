# 文献助手网站

一个智能的学术文献管理平台，提供AI阅读指南生成、智能分类和高效检索功能。

## 功能特性

- 📄 **文献上传与解析** - 支持PDF、Word、Markdown格式文献上传和内容解析
- 🤖 **AI阅读指南生成** - 集成Kimi AI自动生成结构化阅读指南
- 🏷️ **智能分类管理** - 基于内容自动分类和标签管理
- 🔍 **高级搜索功能** - 多条件组合搜索和全文检索
- 📥 **下载与导出** - 支持文献原文下载和阅读指南导出
- ⚡ **批量导入** - 支持多文件批量导入，实时进度显示

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.5.0
- **工具库**: Hutool、Knife4j
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **AI集成**: OkHttp + Kimi AI API
- **文件处理**: Apache PDFBox、POI

### 前端技术
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **SSE通信**: EventSource
- **Markdown渲染**: marked

## 项目结构

```
literature-assistant/
├── src/main/java/com/literature/assistant/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器层
│   ├── entity/          # 实体类
│   ├── service/         # 服务层
│   ├── util/           # 工具类
│   └── LiteratureAssistantApplication.java
├── frontend/            # 前端项目
│   ├── src/
│   │   ├── api/         # API接口
│   │   ├── components/  # 组件
│   │   ├── views/       # 页面视图
│   │   ├── stores/      # 状态管理
│   │   └── utils/       # 工具函数
│   └── package.json
└── README.md
```

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

1. 创建MySQL数据库
```sql
CREATE DATABASE literature_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置数据库连接
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/literature_db
    username: root
    password: root
```

3. 启动后端服务
```bash
mvn clean install
mvn spring-boot:run
```

### 前端启动

1. 安装依赖
```bash
cd frontend
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 构建生产版本
```bash
npm run build
```

## API文档

启动后端服务后，访问以下地址查看API文档：
- Knife4j文档: http://localhost:8080/api/doc.html
- Swagger文档: http://localhost:8080/api/swagger-ui.html

## 配置说明

### 文件上传配置
```yaml
file:
  upload:
    path: ./uploads/          # 文件存储路径
    max-size: 50MB            # 最大文件大小
    allowed-extensions: .pdf,.doc,.docx,.md,.txt
```

### AI服务配置
```yaml
ai:
  kimi:
    base-url: https://api.moonshot.cn/v1
    timeout: 30000
    max-tokens: 4096
    temperature: 0.7
```

## 数据库设计

### 文献表 (literature)
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT | 主键ID |
| title | VARCHAR(255) | 文献标题 |
| file_path | VARCHAR(500) | 文件存储路径 |
| original_file_name | VARCHAR(255) | 原始文件名 |
| file_size | BIGINT | 文件大小 |
| file_type | VARCHAR(10) | 文件类型 |
| tags | VARCHAR(500) | 标签 |
| description | TEXT | 文献描述 |
| reading_guide | LONGTEXT | 阅读指南 |
| category | VARCHAR(100) | 分类 |
| author | VARCHAR(200) | 作者 |
| publish_year | INT | 出版年份 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除标志 |

## 使用说明

1. **上传文献**: 点击"导入文献"按钮，选择文件并输入Kimi AI API密钥
2. **批量导入**: 点击"批量导入"按钮，可同时上传多个文件
3. **搜索文献**: 使用顶部搜索框按分类、描述、指南等条件搜索
4. **查看详情**: 点击文献行的"查看"按钮查看详细信息和阅读指南
5. **下载文献**: 点击"下载"按钮下载原始文献文件

## 开发说明

### 添加新的文件解析器

1. 在 `FileParser` 类中添加新的解析方法
2. 在 `FileUtil` 中注册支持的文件类型
3. 更新前端上传组件的accept属性

### 集成其他AI服务

1. 实现新的 `AIService` 接口
2. 在配置文件中添加相应的配置项
3. 更新服务调用逻辑

## 许可证

MIT License

## 支持

如有问题或建议，请提交Issue或联系开发团队。