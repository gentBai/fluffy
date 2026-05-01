# PROJECT KNOWLEDGE BASE

**生成时间:** 2026-04-30
**分支:** main

## 项目概述

Fluffy（取自《哈利·波特》中的三头犬）是一个基于 **Vert.x 5.0.10** 的 API 网关项目，使用 Java 11 开发，Maven 构建。

项目当前状态：**核心网关组件已全部接入 `MainVerticle` 的 HTTP 请求处理流程**，包括：安全黑白名单检查 → 路由匹配 → 认证（JWT/API Key）→ 限流 → 负载均衡 → 后端转发 → 异步访问日志记录。Admin API 服务器运行在 8889 端口，提供完整的管理后台 REST 接口。

项目包含两部分：
- **后端网关** (`src/main/java`) — Vert.x 响应式 HTTP 服务器
- **管理后台** (`admin/`) — Vue 3 + Element Plus 单页应用，用于配置网关规则

## 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 核心框架 | Vert.x | 5.0.10 | 通过 `vertx-stack-depchain` BOM 管理 |
| 数据库客户端 | vertx-mysql-client | 5.0.10 | 响应式 MySQL 客户端（**注意：README 中提到的 MyBatis-Plus 和 HikariCP 并未实际引入**） |
| 缓存 | Redis | 6.x | 通过 `vertx-redis-client` 连接 |
| 认证 | vertx-auth-jwt | 5.0.10 | JWT 令牌签发与验证 |
| 会话存储 | vertx-web-sstore-cookie / redis | 5.0.10 | 依赖已引入，但代码中暂无会话存储实现 |
| 健康检查 | vertx-health-check | 5.0.10 | 依赖已引入 |
| 前端框架 | Vue | 3.4.x | 管理后台 |
| UI 组件库 | Element Plus | 2.5.x | 管理后台 |
| 构建工具 | Maven | 3.x | `pom.xml` + `mvnw` |
| Java 版本 | OpenJDK | 11 | `maven-compiler-plugin` 配置 `<release>11</release>` |

## 项目结构

```
fluffy/
├── src/main/java/com/halfhex/fluffy/
│   ├── Application.java              # 入口：创建 Vert.x 实例，依次部署 ConfigVerticle → MainVerticle
│   ├── MainVerticle.java             # 网关 HTTP 服务器（8888）+ Admin API（8889），完整请求处理流水线
│   ├── config/
│   │   ├── ConfigHolder.java         # 单例配置容器，读取 application.conf
│   │   └── ConfigVerticle.java       # 读取配置、创建 MySQL 连接池和 Redis 客户端
│   ├── entity/                       # 数据库实体 POJO（10 个类）
│   │   ├── GatewayRoute.java         # 路由规则（含 Builder）
│   │   ├── GatewayService.java       # 后端服务定义（含 Builder）
│   │   ├── ServiceInstance.java      # 服务实例（含 Builder）
│   │   ├── ApiKey.java               # API Key 实体（可变 POJO）
│   │   ├── RateLimitRule.java        # 限流规则实体（可变 POJO）
│   │   ├── Blacklist.java
│   │   ├── Whitelist.java
│   │   ├── CircuitBreakerState.java
│   │   ├── LoadBalanceStrategy.java
│   │   └── AccessLog.java            # 访问日志实体
│   ├── gateway/                      # 网关核心逻辑
│   │   ├── RouteHandler.java         # 路由匹配，内存缓存，每 30 秒刷新
│   │   ├── LoadBalancer.java         # 加权轮询 / 加权随机负载均衡
│   │   └── BackendClient.java        # WebClient 转发请求到后端（保留请求体）
│   ├── repository/                   # 数据访问层（10 个 Repository）
│   │   └── *Repository.java          # 统一使用 `Pool` + `Promise` 回调风格，支持软删除
│   └── security/                     # 安全层（已接入 MainVerticle 请求流水线）
│       ├── ApiKey.java               # API Key 值对象（不可变，含 Builder）
│       ├── ApiKeyHandler.java        # API Key 校验（MySQL + Redis 5 分钟缓存）
│       ├── JwtConfig.java            # JWT 配置值对象
│       ├── JwtHandler.java           # JWT 签发与验证，每 60 秒刷新配置
│       ├── RateLimiter.java          # Redis 滑动窗口限流（Lua 脚本）
│       ├── RateLimitRule.java        # 限流规则值对象（不可变）
│       ├── RateLimitType.java        # 枚举：IP / USERNAME / GLOBAL
│       ├── SecurityChecker.java      # 黑白名单检查（Redis 60 秒缓存）
│       ├── SecurityCheckResult.java  # 枚举：ALLOWED / BLOCKED / BYPASSED
│       └── TargetType.java           # 枚举：IP / USER / API_KEY
│
├── src/test/java/com/halfhex/fluffy/ # 10+ 个测试类
├── src/main/resources/
│   └── application.conf              # JSON 格式配置文件（db / redis / app / gateway）
├── admin/                            # 管理后台前端（Vue 3 + Vite）
│   ├── src/
│   │   ├── main.js                   # Vue 应用入口
│   │   ├── App.vue
│   │   ├── router/index.js           # 路由守卫，未登录重定向到 /login
│   │   ├── api/index.js              # Axios 实例，baseURL: /api/admin，Bearer Token 拦截器
│   │   └── views/                    # Login / Dashboard / Routes / Services / Auth / RateLimit / Blacklist / Logs
│   ├── package.json
│   └── vite.config.js                # 开发服务器端口 3000，代理 /api → localhost:8889
├── sql/
│   └── V001__init_schema.sql         # MariaDB 10.11 兼容的初始化脚本，11 张表 + JWT 初始数据
├── docs/
│   ├── modules/                      # 7 个模块的详细设计文档（中文）
│   └── superpowers/
│       └── specs/
│           └── 2026-04-19-gateway-design.md  # 总体架构设计文档
├── openspec/                         # OpenSpec 工作流配置（spec-driven，当前无活跃变更）
│   ├── config.yaml
│   ├── changes/
│   └── specs/
├── pom.xml                           # Maven 构建配置
├── package.json                      # 根目录仅包含 oh-my-opencode 依赖（用于 OpenSpec CLI）
└── .editorconfig                     # 2 空格缩进，UTF-8，LF 换行
```

## 构建与运行命令

```bash
# 运行测试
./mvnw clean test

# 运行单个测试类
./mvnw test -Dtest=TestMainVerticle

# 打包（生成 fat JAR）
./mvnw clean package

# 运行应用（通过 Maven exec 插件，使用 io.vertx.core.Launcher）
./mvnw clean compile exec:java

# 直接运行入口类（Application.main）
# 需要在 IDE 中运行，或使用 java -cp 命令行指定 classpath
```

打包后会生成 `target/fluffy-1.0.0-SNAPSHOT-fat.jar`，其 Manifest 中：
- `Main-Class`: `io.vertx.core.Launcher`
- `Main-Verticle`: `com.halfhex.fluffy.MainVerticle`

### 管理后台

```bash
cd admin
npm run dev      # 开发服务器，端口 3000，代理 /api 到 localhost:8889
npm run build    # 构建静态文件到 admin/dist/
npm run preview  # 预览生产构建
```

## 数据库架构

使用 **MariaDB 10.11+**，共 11 张表，定义见 `sql/V001__init_schema.sql`：

| 类别 | 表名 |
|------|------|
| 核心 | `gateway_route`, `gateway_service`, `service_instance`, `load_balance_strategy` |
| 认证 | `api_key`, `jwt_config` |
| 限流 | `rate_limit_rule` |
| 安全 | `blacklist`, `whitelist` |
| 日志/监控 | `access_log`, `circuit_breaker_state` |

Schema 设计约定：
- 使用 `deleted` (TINYINT) 软删除，而非 `enabled` 字段
- 使用 `DATETIME` 而非 `TIMESTAMP`
- 枚举值使用 `VARCHAR` 存储，不使用数据库原生 ENUM
- **不设置物理外键**
- 默认字符集 `utf8mb4_unicode_ci`

初始数据：`jwt_config` 表中已插入一条默认配置（issuer: `fluffy-gateway`，algorithm: `HS256`）。

## 代码风格与约定

- **包名**: `com.halfhex.fluffy`
- **缩进**: 2 个空格（来自 `.editorconfig`）
- **换行**: LF（Unix 风格）
- **编码**: UTF-8
- **Java 版本**: 11
- **异步模式**: 标准 Vert.x 5 的 `Promise<T>` / `Future<T>` / `Handler<AsyncResult<T>>`（**注意：旧版 AGENTS.md 中提到的 `CompletableFuture<Void>` 返回模式不存在**）
- **测试方法命名**: snake_case，例如 `deploy_verticle`, `verticle_deployed`
- **Repository 风格**: 方法签名接受 `Promise<T>` 参数，通过 `client.withConnection()` 执行预编译 SQL，使用 `onComplete` 回调完成 Promise
- **实体映射**: 部分表使用 Builder 模式（`GatewayRoute`, `GatewayService`, `ServiceInstance`），其余为普通 POJO

## 测试策略

测试框架：**JUnit 5** + **Vert.x JUnit 5 扩展** (`vertx-junit5`) + **Mockito**

现有测试文件（7 个）：

| 测试类 | 测试内容 |
|--------|----------|
| `TestMainVerticle` | Verticle 部署冒烟测试 |
| `ConfigHolderTest` | 配置解析与默认值 |
| `GatewayRouteTest` | Builder 模式、HttpMethod 枚举 |
| `ServiceInstanceTest` | Builder 模式、Status 枚举 |
| `BackendClientTest` | 反射检查静态常量集合 |
| `RateLimiterTest` | 构造函数、枚举、Getter |
| `SecurityEnumsTest` | 安全相关枚举值验证 |

**测试特点**：
- 以单元测试和结构测试为主
- `RouteHandlerTest` 测试路径匹配逻辑（精确、/*、/**、正则、方法通配符）
- `LoadBalancerTest` 测试加权随机/轮询选择逻辑
- `RateLimiterTest` 包含 Redis Mock Lua 脚本执行测试
- `BackendClientTest` 测试不可变常量集合
- 无 MySQL / Redis 集成测试（部署 MainVerticle 的测试需要本地数据库）

## 已知问题与注意事项

1. **命名冲突**：存在两个 `ApiKey` 类（`entity.ApiKey` 和 `security.ApiKey`），以及两个 `RateLimitRule` 类（`entity.RateLimitRule` 和 `security.RateLimitRule`）。前者是可变的数据库实体，后者是不可变的业务值对象。
2. **断路器仅有数据层**：`CircuitBreakerState` 实体和 Repository 存在，但无运行时熔断逻辑（状态机未接入请求处理流程）。
3. **负载均衡健康追踪为空**：`LoadBalancer.recordSuccess()` 和 `recordFailure()` 是空方法（未实现基于成功/失败率的动态权重调整）。
4. **README 技术栈描述已修正**：README 已更新为 `vertx-mysql-client`，但 AGENTS.md 中仍保留此提醒以防其他文档遗漏。
5. **无 CI/CD**：未配置 GitHub Actions 或其他持续集成流程。
6. **Admin API 日志查询未实现**：`GET /api/admin/logs` 和 `/api/admin/logs/export` 返回空数据或 501。

## 配置说明

运行时读取 `src/main/resources/application.conf`（JSON 格式）：

```json
{
  "db": {
    "host": "localhost",
    "port": 3306,
    "database": "fluffy",
    "username": "root",
    "password": "",
    "maxPoolSize": 20,
    "connectionTimeout": 30000
  },
  "redis": {
    "host": "localhost",
    "port": 6379,
    "maxPoolSize": 10,
    "timeout": 5000
  },
  "app": {
    "port": 8888,
    "adminPort": 8889
  },
  "gateway": {
    "requestTimeout": 30000,
    "maxConcurrentRequests": 10000
  }
}
```

`ConfigHolder` 提供上述所有配置的带默认值访问器。

## 文档与设计资料

- `docs/modules/` — 7 个模块的中文设计文档（路由、负载均衡、安全认证、黑白名单、限流、断路器、配置管理）
- `docs/superpowers/specs/2026-04-19-gateway-design.md` — 总体架构设计文档，包含数据库设计、Verticle 层级、请求处理流程、Admin REST API 设计
- `openspec/` — OpenSpec 规范驱动工作流目录，当前无活跃变更

## 安全考量

- `application.conf` 中的数据库密码为空（默认值）
- `jwt_config` 初始数据的 `secret_key` 是明文占位符（`your-256-bit-secret-key-change-in-production`），**生产环境必须修改**
- 限流器在 Redis 连接异常时采用 **fail-closed** 策略（返回 `allowed = false`）
- API Key 和 JWT Token 从 `Authorization: Bearer <token>` 头或原始头中提取
