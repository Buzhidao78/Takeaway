<div align="center">

# 🍱 BZY 外卖系统

**基于 Spring Boot 3 + Vue 3 的全栈外卖点餐平台**

四大角色 · 支付宝沙箱支付 · WebSocket 实时推送 · 面向接口编程 · Docker 容器化部署

</div>

## ✨ 项目亮点

- 🧑‍🍳 **四大角色一体**：用户 / 商家 / 骑手 / 管理端，前后端权限双层控制
- 💳 **真实支付体验**：集成支付宝沙箱 SDK，完整支付回调闭环
- ⚡ **实时配送**：WebSocket + Redis 分布式锁实现骑手秒级抢单
- 🧩 **架构规范**：后端面向接口编程 + 分层架构 + 全局异常/统一响应
- 📦 **一条命令部署**：Docker Compose 编排，支持离线镜像包部署

## 🚀 功能概览

| 端 | 核心功能 |
|----|---------|
| **用户端** | 注册登录、商家浏览、搜索、购物车、下单支付、订单管理、地址管理、店铺/菜品评价（含追评、点赞）、消息通知 |
| **商家端** | 店铺信息、营业状态、菜品分类与菜品管理、订单处理、评价回复、配送设置、收益统计 |
| **骑手端** | 注册审核、在线状态、实时抢单、配送流程、收益明细 |
| **管理端** | 用户管理、店铺审核、骑手审核与指派、菜品分类管理 |

## 🛠️ 技术栈

**后端**：Spring Boot 3.2 · Java 17 · MyBatis-Plus · MySQL 8 · Redis · JWT · WebSocket · 支付宝 SDK · @Async 异步任务

**前端**：Vue 3 · Vite 5 · Element Plus · Pinia · Vue Router 4 · Axios

**部署**：Docker · Docker Compose · Nginx

## 📂 文档导航

| 文档 | 说明 |
|------|------|
| [📐 架构设计](docs/01-架构设计.md) | 系统架构、分层设计、核心机制、四大角色、技术选型 |
| [🗄️ 数据库设计](docs/02-数据库设计.md) | 26 张表结构、订单状态机、收益体系、通用约定 |
| [🧭 功能模块划分](docs/03-功能模块划分.md) | 四大角色功能点 → Controller → 前端页面对照 |
| [🚢 部署指南](docs/04-部署指南.md) | Docker 联机/离线部署、Nginx 配置、生产注意 |

## ⚡ 快速开始

### 环境要求
JDK 17+ · Maven 3.8+ · MySQL 8.0 · Redis · Node.js 18+ · Docker（可选）

### 手动运行
```bash
# 1. 初始化数据库
mysql -u root -p bzy_takeaway < sql/00_complete_init.sql

# 2. 启动后端（连接信息改 application-dev.yml）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. 启动前端
cd frontend
npm install
npm run dev
```
访问 `http://localhost:5173`

### Docker 一键运行
```bash
docker compose up -d
```
访问 `http://localhost:8091`

> 完整部署细节（含离线镜像包）见 [部署指南](docs/04-部署指南.md)。

### 测试账号（密码均为 `123456`）
| 角色 | 手机号 |
|------|--------|
| 管理员 | 13800000000 |
| 用户 | 13600000000 |
| 商家 | 13900000000 |
| 骑手 | 13700000000 |

## 📦 项目结构

```
├── frontend/            # Vue 3 前端
│   ├── src/
│   │   ├── api/        # 接口封装
│   │   ├── components/ # 公共组件
│   │   ├── router/     # 路由（hash + 角色守卫）
│   │   ├── stores/     # Pinia 状态管理
│   │   ├── views/      # 页面（user/merchant/rider/admin）
│   │   └── composables/# 组合式函数
│   └── nginx.conf      # 前端 Nginx 反代配置
├── src/main/java/       # 后端源码
│   └── com/bzy/takeaway/
│       ├── controller/ # REST 接口
│       ├── service/    # 业务接口
│       ├── service/impl/ # 业务实现
│       ├── mapper/     # 数据访问
│       ├── entity/     # 实体
│       ├── config/     # 配置
│       └── websocket/  # 实时推送
├── sql/                # 数据库初始化脚本
├── docs/               # 项目文档
├── docker-compose.yml  # 容器编排
├── Dockerfile          # 后端镜像
└── pom.xml
```

## 📄 许可证

MIT License