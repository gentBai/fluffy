-- Fluffy API Gateway Database Schema
-- MariaDB 10.11 compatible
-- Changes: deleted(enabled), datetime, varchar enums, defaults, no physical foreign keys

-- =====================================================
-- Core Tables
-- =====================================================

CREATE TABLE IF NOT EXISTS gateway_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    path_pattern VARCHAR(255) NOT NULL DEFAULT '',
    http_method VARCHAR(20) NOT NULL DEFAULT '*',
    service_id BIGINT DEFAULT NULL,
    auth_required TINYINT(1) NOT NULL DEFAULT 0,
    rate_limit_enabled TINYINT(1) NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 0,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_path_method (path_pattern, http_method),
    INDEX idx_deleted (deleted),
    INDEX idx_service (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gateway_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    base_url VARCHAR(255) NOT NULL DEFAULT '',
    health_check_url VARCHAR(255) DEFAULT '',
    health_check_interval INT NOT NULL DEFAULT 30,
    max_connections INT NOT NULL DEFAULT 200,
    timeout_ms INT NOT NULL DEFAULT 5000,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS service_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id BIGINT NOT NULL DEFAULT 0,
    host VARCHAR(255) NOT NULL DEFAULT '',
    port INT NOT NULL DEFAULT 80,
    weight INT NOT NULL DEFAULT 100,
    healthy TINYINT(1) NOT NULL DEFAULT 1,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    last_heartbeat DATETIME DEFAULT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service (service_id),
    INDEX idx_healthy (healthy),
    INDEX idx_enabled (enabled),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS load_balance_strategy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(100) NOT NULL DEFAULT '',
    algorithm VARCHAR(20) NOT NULL DEFAULT 'ROUND_ROBIN',
    config JSON DEFAULT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_service_deleted (service_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Authentication Tables
-- =====================================================

CREATE TABLE IF NOT EXISTS api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_value VARCHAR(64) NOT NULL DEFAULT '',
    name VARCHAR(100) NOT NULL DEFAULT '',
    user_id BIGINT DEFAULT NULL,
    route_ids JSON DEFAULT NULL,
    rate_limit_per_minute INT NOT NULL DEFAULT 1000,
    active TINYINT(1) NOT NULL DEFAULT 1,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    expires_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_key_deleted (key_value, deleted),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS jwt_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issuer VARCHAR(100) NOT NULL DEFAULT '',
    secret_key VARCHAR(500) NOT NULL DEFAULT '',
    algorithm VARCHAR(20) NOT NULL DEFAULT 'HS256',
    access_token_ttl_sec INT NOT NULL DEFAULT 3600,
    refresh_token_ttl_sec INT NOT NULL DEFAULT 86400,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Rate Limiting Tables
-- =====================================================

CREATE TABLE IF NOT EXISTS rate_limit_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    limit_type VARCHAR(20) NOT NULL DEFAULT 'GLOBAL',
    requests_per_minute INT DEFAULT NULL,
    requests_per_hour INT DEFAULT NULL,
    requests_per_day INT DEFAULT NULL,
    route_id BIGINT DEFAULT NULL,
    service_id BIGINT DEFAULT NULL,
    max_requests INT DEFAULT NULL,
    burst_size INT NOT NULL DEFAULT 10,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_limit_type (limit_type),
    INDEX idx_route (route_id),
    INDEX idx_service (service_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Security Tables
-- =====================================================

CREATE TABLE IF NOT EXISTS blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL DEFAULT 'IP',
    target_value VARCHAR(255) NOT NULL DEFAULT '',
    reason VARCHAR(500) DEFAULT '',
    expires_at DATETIME DEFAULT NULL,
    created_by VARCHAR(100) DEFAULT '',
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_value),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS whitelist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL DEFAULT 'IP',
    target_value VARCHAR(255) NOT NULL DEFAULT '',
    description VARCHAR(500) DEFAULT '',
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_target_unique (target_type, target_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Logging Tables
-- =====================================================

CREATE TABLE IF NOT EXISTS access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_id VARCHAR(64) DEFAULT '',
    request_method VARCHAR(10) NOT NULL DEFAULT '',
    request_path VARCHAR(500) NOT NULL DEFAULT '',
    request_query TEXT,
    request_body TEXT,
    response_status INT DEFAULT NULL,
    response_time_ms INT DEFAULT NULL,
    client_ip VARCHAR(45) DEFAULT '',
    user_id BIGINT DEFAULT NULL,
    api_key_id BIGINT DEFAULT NULL,
    route_id BIGINT DEFAULT NULL,
    service_id BIGINT DEFAULT NULL,
    instance_id BIGINT DEFAULT NULL,
    error_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_trace (trace_id),
    INDEX idx_client_ip (client_ip),
    INDEX idx_user (user_id),
    INDEX idx_route (route_id),
    INDEX idx_created (created_at),
    INDEX idx_status (response_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS circuit_breaker_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id BIGINT NOT NULL DEFAULT 0,
    instance_id BIGINT DEFAULT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'CLOSED',
    failure_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    last_failure_time DATETIME DEFAULT NULL,
    last_state_change DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_service_instance (service_id, instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Initial Data
-- =====================================================

INSERT INTO jwt_config (issuer, secret_key, algorithm, access_token_ttl_sec, refresh_token_ttl_sec, deleted)
VALUES ('fluffy-gateway', 'your-256-bit-secret-key-change-in-production', 'HS256', 3600, 86400, 0);
