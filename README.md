# BZY 外卖系统

基于 Spring Boot 3 + Vue 3 的全栈外卖点餐平台，支持用户端、商家端、骑手端和管理端四个角色，集成支付宝沙箱支付、WebSocket 实时推送、Redis 分布式锁等核心功能。

## 功能概览

| 模块 | 核心功能 |
|------|---------|
| 用户端 | 注册登录、商家浏览、购物车、下单支付、订单管理、评价 |
| 商家端 | 店铺管理、菜品 CRUD、订单处理、评价回复、收益统计 |
| 骑手端 | 注册审核、在线状态、抢单配送、收益明细 |
| 管理端 | 用户管理、店铺/骑手审核、系统监控 |

## 技术栈

**后端**

| 技术 | 说明 |
|------|------|
| Java 17 + Spring Boot 3.2 | 后端框架 |
| MyBatis-Plus 3.5 | ORM |
| MySQL 8.0 | 数据库 |
| Redis | 缓存、分布式锁、登录限流 |
| JWT | 认证授权 |
| WebSocket | 骑手抢单实时推送 |
| 支付宝 SDK | 沙箱支付 |

**前端**

| 技术 | 说明 |
|------|------|
| Vue 3 + Vite 5 | 前端框架与构建工具 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由 |
| Axios | HTTP 请求 |

## 项目结构

```
├── frontend/                  # Vue 3 前端
│   ├── src/
│   │   ├── api/              # 接口封装
│   │   ├── components/       # 公共组件
│   │   ├── composables/      # 组合式函数
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── views/            # 页面（用户/商家/骑手/管理端）
│   │   └── utils/            # 工具函数
│   └── package.json
├── src/main/java/com/bzy/takeaway/
│   ├── controller/           # REST 控制器
│   ├── service/              # 业务逻辑
│   ├── mapper/               # MyBatis Mapper
│   ├── entity/               # 实体类
│   ├── config/               # 配置类
│   ├── interceptor/          # 认证/权限拦截器
│   ├── websocket/            # WebSocket 处理
│   └── common/               # 公共类（Result、常量）
├── src/main/resources/
│   ├── mapper/               # MyBatis XML
│   ├── db/migration/         # 数据库迁移脚本
│   └── application*.yml      # 多环境配置
├── sql/                      # 数据库初始化脚本
├── docker-compose.yml        # Docker 编排
├── Dockerfile                # 后端镜像
└── pom.xml                   # Maven 依赖
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Redis 6+
- Node.js 18+

### 1. 克隆项目

```bash
git clone https://github.com/Buzhidao78/Takeaway.git
cd Takeaway
```

### 2. 初始化数据库

```sql
CREATE DATABASE bzy_takeaway CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
mysql -u root -p bzy_takeaway < sql/00_complete_init.sql
```

### 3. 配置后端

编辑 `src/main/resources/application-dev.yml`，修改数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bzy_takeaway?...
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动后端

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

### 测试账号

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 管理员 | 13800000000 | 123456 |
| 商家 | 13900000000 | 123456 |
| 用户 | 13600000000 | 123456 |
| 骑手 | 13700000000 | 123456 |

## Docker 部署

```bash
# 构建前端并打包后端
mvn clean package -DskipTests
cd frontend && npm install && npm run build && cd ..

# 启动所有服务（MySQL + Redis + Backend）
docker compose up -d
```

服务启动后访问 `http://localhost:8080`。

## 核心设计

### 认证授权
- JWT 无状态认证，Token 有效期 24 小时
- BCrypt 密码加密
- 登录失败 5 次锁定账号 30 分钟（Redis 实现）

### 订单流程
```
待支付 → 已支付 → 制作中 → 配送中 → 已完成
  ↓                        ↓
已取消                   已退款
```

### 骑手抢单
- Redis 分布式锁防止重复抢单
- WebSocket 实时推送新订单
- 定时任务处理抢单超时

### 前后端约定
- 后端统一返回 `{ code, message, data }` 结构
- 前端 Axios 拦截器自动解包，调用方直接拿到 `data`
- 所有 API 统一 `/api` 前缀

## 许可证

MIT License
