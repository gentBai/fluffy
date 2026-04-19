# Fluffy API Gateway Design

**Date:** 2026-04-19
**Author:** Sisyphus
**Status:** Draft

## 1. Overview

Fluffy is a Vert.x-based API Gateway with an admin dashboard for configuration management. It provides routing, load balancing, authentication, rate limiting, blacklists/whitelists, and logging/monitoring.

**Tech Stack:**
- Backend: Vert.x 5.0.10, Java 11, MariaDB 10.11, Redis
- Frontend: Vue 3 + Element Plus
- Build: Maven

## 2. Database Schema

### 2.1 Core Tables

#### gateway_route (路由规则)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 路由ID |
| name | VARCHAR(100) | NOT NULL | 路由名称 |
| path_pattern | VARCHAR(255) | NOT NULL | 路径模式 (e.g. /api/users/*) |
| http_method | VARCHAR(20) | NOT NULL DEFAULT '*' | HTTP方法 |
| service_id | BIGINT | FK | 关联服务ID |
| auth_required | BOOLEAN | DEFAULT FALSE | 是否需要认证 |
| rate_limit_enabled | BOOLEAN | DEFAULT FALSE | 是否启用限流 |
| priority | INT | DEFAULT 0 | 优先级 |
| enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### gateway_service (后端服务)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 服务ID |
| name | VARCHAR(100) | NOT NULL | 服务名称 |
| base_url | VARCHAR(255) | NOT NULL | 后端基础URL |
| health_check_url | VARCHAR(255) | | 健康检查URL |
| health_check_interval | INT | DEFAULT 30 | 检查间隔(秒) |
| max_connections | INT | DEFAULT 200 | 最大连接数 |
| timeout_ms | INT | DEFAULT 5000 | 超时毫秒 |
| enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### service_instance (服务实例)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 实例ID |
| service_id | BIGINT | FK | 所属服务ID |
| host | VARCHAR(255) | NOT NULL | 主机地址 |
| port | INT | NOT NULL | 端口 |
| weight | INT | DEFAULT 100 | 权重(1-100) |
| status | ENUM | DEFAULT 'HEALTHY' | HEALTHY/UNHEALTHY/DOWN |
| active | BOOLEAN | DEFAULT TRUE | 是否活跃 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### load_balance_strategy (负载均衡策略)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 策略ID |
| route_id | BIGINT | FK, UNIQUE | 关联路由ID |
| strategy_type | ENUM | NOT NULL | ROUND_ROBIN/RANDOM/CONSISTENT_HASH |
| config | JSON | | 策略配置(哈希参数等) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2.2 Authentication Tables

#### api_key (API Key认证)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Key ID |
| key_value | VARCHAR(64) | NOT NULL, UNIQUE | Key值(SHA256) |
| name | VARCHAR(100) | | 密钥名称 |
| user_id | BIGINT | | 关联用户ID |
| route_ids | JSON | | 可访问路由IDs |
| rate_limit_per_minute | INT | DEFAULT 1000 | 每分钟限流 |
| enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| expires_at | TIMESTAMP | | 过期时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### jwt_config (JWT配置)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 配置ID |
| issuer | VARCHAR(100) | NOT NULL | 发行者 |
| secret_key | VARCHAR(500) | NOT NULL | 密钥(加密存储) |
| algorithm | VARCHAR(20) | DEFAULT 'HS256' | 算法 |
| access_token_ttl_sec | INT | DEFAULT 3600 | Access Token过期秒 |
| refresh_token_ttl_sec | INT | DEFAULT 86400 | Refresh Token过期秒 |
| enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2.3 Rate Limiting Tables

#### rate_limit_rule (限流规则)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 规则ID |
| name | VARCHAR(100) | NOT NULL | 规则名称 |
| limit_type | ENUM | NOT NULL | IP/USERNAME/GLOBAL |
| requests_per_minute | INT | | 每分钟请求数 |
| requests_per_hour | INT | | 每小时请求数 |
| requests_per_day | INT | | 每天请求数 |
| route_id | BIGINT | FK | 关联路由(空=全局) |
| burst_size | INT | DEFAULT 10 | 突发容量 |
| enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2.4 Security Tables

#### blacklist (黑名单)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 记录ID |
| target_type | ENUM | NOT NULL | IP/USER/API_KEY |
| target_value | VARCHAR(255) | NOT NULL | 目标值 |
| reason | VARCHAR(500) | | 封禁原因 |
| expires_at | TIMESTAMP | | 解封时间(空=永久) |
| created_by | VARCHAR(100) | | 操作人 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### whitelist (白名单)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 记录ID |
| target_type | ENUM | NOT NULL | IP/USER/API_KEY |
| target_value | VARCHAR(255) | NOT NULL | 目标值 |
| description | VARCHAR(500) | | 描述 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 2.5 Logging Tables

#### access_log (访问日志)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 日志ID |
| trace_id | VARCHAR(64) | INDEX | 追踪ID |
| request_method | VARCHAR(10) | NOT NULL | 请求方法 |
| request_path | VARCHAR(500) | NOT NULL | 请求路径 |
| request_query | TEXT | | 查询参数 |
| request_body | TEXT | | 请求体 |
| response_status | INT | | 响应状态码 |
| response_time_ms | INT | | 响应时间(毫秒) |
| client_ip | VARCHAR(45) | INDEX | 客户端IP |
| user_id | BIGINT | INDEX | 用户ID |
| api_key_id | BIGINT | INDEX | API Key ID |
| route_id | BIGINT | FK | 匹配的路由ID |
| service_id | BIGINT | FK | 转发的服务ID |
| instance_id | BIGINT | FK | 转发的实例ID |
| error_message | TEXT | | 错误信息 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP, INDEX | 请求时间 |

#### circuit_breaker_state (断路器状态)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 记录ID |
| service_id | BIGINT | FK | 服务ID |
| instance_id | BIGINT | FK | 实例ID(可空) |
| state | ENUM | DEFAULT 'CLOSED' | CLOSED/OPEN/HALF_OPEN |
| failure_count | INT | DEFAULT 0 | 连续失败次数 |
| success_count | INT | DEFAULT 0 | 连续成功次数 |
| last_failure_time | TIMESTAMP | | 最后失败时间 |
| last_state_change | TIMESTAMP | | 最后状态变更 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

## 3. Backend Architecture

### 3.1 Verticle Structure

```
MainVerticle (入口)
├── RouterVerticle (路由配置管理)
│   ├── RouteManager (路由表管理)
│   └── ServiceDiscovery (服务发现)
├── ProxyVerticle (请求代理)
│   ├── RouteHandler (路由匹配)
│   ├── LoadBalancer (负载均衡)
│   ├── BackendClient (后端调用)
│   └── CircuitBreaker (断路器)
├── AuthVerticle (认证鉴权)
│   ├── JwtHandler (JWT验证)
│   ├── ApiKeyHandler (API Key验证)
│   └── PermissionChecker (权限检查)
├── RateLimitVerticle (限流)
│   ├── IpRateLimiter (IP限流)
│   ├── UserRateLimiter (用户限流)
│   └── GlobalRateLimiter (全局限流)
├── SecurityVerticle (黑白名单)
│   ├── BlacklistChecker (黑名单)
│   └── WhitelistChecker (白名单)
├── LogVerticle (日志监控)
│   ├── AccessLogger (访问日志)
│   ├── MetricsCollector (指标收集)
│   └── AlertManager (告警管理)
└── AdminVerticle (管理后台API)
    ├── RouteAdminAPI
    ├── ServiceAdminAPI
    ├── AuthAdminAPI
    ├── RateLimitAdminAPI
    └── LogQueryAPI
```

### 3.2 Request Flow

```
请求 → SecurityVerticle (黑白名单)
     → AuthVerticle (JWT/API Key验证)
     → RateLimitVerticle (限流检查)
     → ProxyVerticle (路由+负载+断路)
     → LogVerticle (记录日志)
     → 响应
```

### 3.3 Load Balancing Strategies

1. **ROUND_ROBIN** - 轮询
2. **RANDOM** - 随机
3. **CONSISTENT_HASH** - 一致性哈希 (后续支持)

## 4. Frontend Architecture

### 4.1 Pages

| Module | Description |
|--------|-------------|
| Dashboard | 实时监控指标、服务健康状态、告警列表 |
| Route Management | CRUD路由规则、路径模式、优先级 |
| Service Management | 后端服务配置、健康检查、实例管理 |
| Load Balancing | 策略配置、权重调整 |
| Authentication | API Key管理、JWT配置 |
| Rate Limiting | 限流规则配置 |
| Blacklist/Whitelist | IP/User/API Key黑白名单 |
| Logs | 访问日志查询、错误追踪 |

### 4.2 Dashboard Metrics

- 请求量趋势图
- 响应时间分布
- 错误率
- 活跃连接数
- 服务健康状态

## 5. Implementation Phases

### Phase 1: Database Schema
- Create SQL migration scripts
- Setup MySQL/MariaDB schema
- Setup Redis connection

### Phase 2: Core Gateway Infrastructure
- MainVerticle refactoring
- Config loading from database
- Basic routing framework

### Phase 3: Routing & Load Balancing
- Dynamic route loading
- Load balancer implementations
- Health check system
- Circuit breaker

### Phase 4: Auth & Security
- JWT authentication
- API Key authentication
- Blacklist/Whitelist
- Rate limiting

### Phase 5: Admin Frontend
- Vue 3 project setup
- Element Plus integration
- API integration
- Dashboard

## 6. API Endpoints (Admin)

### Routes
- GET/POST /admin/routes
- GET/PUT/DELETE /admin/routes/:id

### Services
- GET/POST /admin/services
- GET/PUT/DELETE /admin/services/:id
- GET/POST /admin/services/:id/instances

### Authentication
- GET/POST /admin/api-keys
- GET/PUT/DELETE /admin/api-keys/:id
- GET/POST /admin/jwt-config

### Rate Limiting
- GET/POST /admin/rate-limit-rules
- GET/PUT/DELETE /admin/rate-limit-rules/:id

### Blacklist/Whitelist
- GET/POST /admin/blacklist
- DELETE /admin/blacklist/:id
- GET/POST /admin/whitelist
- DELETE /admin/whitelist/:id

### Logs
- GET /admin/logs (with pagination & filters)

## 7. Configuration

### Database (application.conf)
```json
{
  "db": {
    "host": "localhost",
    "port": 3306,
    "database": "fluffy",
    "username": "root",
    "password": ""
  },
  "redis": {
    "host": "localhost",
    "port": 6379
  },
  "app": {
    "port": 8888,
    "adminPort": 8889
  }
}
```
