# Vcampus · 虚拟校园管理系统

一个基于 Java Swing + TCP Socket + MySQL 的三层架构校园管理系统，覆盖认证鉴权、学籍管理、选课、图书馆、商店与 AI 智能助理等模块。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Java 21 / Swing / FlatLaf |
| 通信 | TCP Socket + Java 对象序列化（端口 8888） |
| 后端 | 多线程 Socket 服务端 + DAO/Service 分层 |
| 数据库 | MySQL 8+ |
| 构建 | Maven |
| 邮件 | JavaMail（QQ SMTP，用于找回密码） |
| AI | DeepSeek Chat API |

## 功能模块

- **认证 & RBAC**：SHA-256 密码哈希登录；基于角色-权限映射（`permission` + `role_permission` 表）的细粒度访问控制，50 个权限点覆盖全部写/管理操作；找回密码（邮箱验证码）。
- **学籍管理**：学生 CRUD、个人资料审核工作流（PENDING→APPROVED/REJECTED）。
- **选课系统**：课程/教学班/上课时间 CRUD、选退课（容量+时间冲突校验）、课表渲染。
- **图书馆**：图书/期刊 CRUD、借还（逾期罚款）、借阅记录。
- **商店**：商品/分类 CRUD、下单（原子扣库存防超卖）、订单状态流转。
- **AI 智能助理**：全局浮动按钮 → 按当前模块自动切换上下文（选课/图书馆/商店/教师/通用），数据集注入 + 滑动窗口记忆。

## 快速开始

### 前置条件

- JDK 21+
- Maven 3.8+
- MySQL 8+（或 9.x）

### 1. 初始化数据库

```bash
mysql -uroot -p < Vcampus/src/vcampus.sql
```

脚本会创建 `vcampus` 库、19 张表（含 RBAC 权限表）及种子数据。

### 2. 配置

复制模板并填入真实值：

```bash
cp Vcampus/src/config.example.properties Vcampus/src/config.properties
```

编辑 `config.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/vcampus?useSSL=false&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=你的MySQL密码

deepseek.api.key=你的DeepSeek API Key    # 留空则 AI 助理不可用

mail.username=你的QQ邮箱@qq.com            # 找回密码发件人
mail.password=QQ邮箱授权码                 # 非 QQ 登录密码
mail.from=你的QQ邮箱@qq.com
```

> `config.properties` 已被 `.gitignore` 忽略，不会提交。

### 3. 构建与测试

```bash
cd Vcampus
mvn clean test
```

### 4. 运行

**启动服务端**（监听 8888）：

```bash
mvn exec:java -Dexec.mainClass=server.net.ServerMain
```

**启动客户端**：

```bash
mvn exec:java -Dexec.mainClass=App
```

或用 IDE 直接运行 `server.net.ServerMain` 和 `App`。

### 默认账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin1 | 123456 | 管理员 |
| teacher1 | 123456 | 教师 |
| student1 | 123456 | 学生 |

## 项目结构

```
Vcampus/
├── pom.xml                      # Maven 构建配置
├── src/
│   ├── App.java                # 客户端入口
│   ├── DatabaseChecker.java    # 数据库连通性检查工具
│   ├── vcampus.sql             # 完整建库脚本（19 表 + 种子）
│   ├── config.example.properties  # 配置模板
│   ├── config.properties       # 本地配置（gitignore）
│   ├── client/                 # 客户端
│   │   ├── ai/                 # AI 助理（hooks 工厂 + LLM 封装）
│   │   ├── controller/         # 8 个领域控制器
│   │   ├── net/ClientSocket     # 持久连接 + 自动重连
│   │   └── ui/                 # ~40 个 Swing 面板/对话框
│   ├── common/                 # 客户端/服务端共享
│   │   ├── model/              # 16 个序列化 POJO
│   │   ├── net/Message         # 通信信封（type/data/extra/caller）
│   │   ├── net/MessageType     # 85 个消息类型常量
│   │   └── Permissions         # 50 个权限码常量
│   ├── server/                 # 服务端
│   │   ├── dao/                # 14 个 DAO（接口+实现）
│   │   ├── net/ServerThread    # 分发器 → 6 个域 Handler
│   │   └── service/           # 业务逻辑（含 AuthService RBAC）
│   └── util/                  # 工具类（Config/DBUtil/EncryptUtil/
│       │                        UITheme/AsyncRunner/MailSender 等）
│   └── pictures/              # UI 图标与背景图
└── test/                       # JUnit 5 单元测试
```

## 架构概览

```
客户端 (Swing)                         服务端
┌─────────────┐    TCP 8888     ┌──────────────────┐    JDBC    ┌────────┐
│  UI 面板     │ ◀────────────▶ │  ServerThread     │ ◀────────▶ │ MySQL  │
│  Controller  │  Message 对象   │   ├ UserHandler   │            │ vcampus│
│  ClientSocket│  (type/data/    │   ├ StudentHandler│            └────────┘
│  (持久连接)   │   caller/extra) │   ├ CourseHandler  │
│  ClientSession│                 │   ├ BookHandler   │
│  (RBAC caller)│                 │   ├ JournalHandler│
│  AgentPanel  │                 │   └ ShopHandler   │
│  (AI 助理)    │                 │       └ AuthService│
└─────────────┘                 │          (RBAC 校验) │
                                 └──────────────────┘
```

- **通信协议**：`Message` 对象含 `type`（消息类型）、`data`（业务载荷）、`extra`（附加数据）、`caller`（调用方身份，RBAC 用）。客户端 `ClientSocket` 单例持久连接 + 自动重连；`ClientSession` 在登录后自动注入 `caller`。
- **RBAC**：登录后客户端持有 `ClientSession`，每次请求自动携带 `caller`；服务端各 Handler 对写/管理操作用 `AuthService.hasPermission(role, permCode)` 校验，无权限返回 fail。
- **AI 助理**：全量上下文注入模式——每次提问把当前模块的实时 DB 数据序列化为 JSON `<<DATASET>>` 注入 system prompt，非经典 RAG（无向量检索）；数据量小、实时性好。
