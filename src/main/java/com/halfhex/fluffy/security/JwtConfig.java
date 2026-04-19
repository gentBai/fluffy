package com.halfhex.fluffy.security;

public final class JwtConfig {

    private final Long id;
    private final String issuer;
    private final String secretKey;
    private final String algorithm;
    private final Integer accessTokenTtlSec;
    private final Integer refreshTokenTtlSec;
    private final Boolean enabled;

    private JwtConfig(Builder builder) {
        this.id = builder.id;
        this.issuer = builder.issuer;
        this.secretKey = builder.secretKey;
        this.algorithm = builder.algorithm;
        this.accessTokenTtlSec = builder.accessTokenTtlSec;
        this.refreshTokenTtlSec = builder.refreshTokenTtlSec;
        this.enabled = builder.enabled;
    }

    public Long getId() { return id; }
    public String getIssuer() { return issuer; }
    public String getSecretKey() { return secretKey; }
    public String getAlgorithm() { return algorithm; }
    public Integer getAccessTokenTtlSec() { return accessTokenTtlSec; }
    public Integer getRefreshTokenTtlSec() { return refreshTokenTtlSec; }
    public Boolean getEnabled() { return enabled; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String issuer;
        private String secretKey;
        private String algorithm;
        private Integer accessTokenTtlSec;
        private Integer refreshTokenTtlSec;
        private Boolean enabled;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder issuer(String issuer) { this.issuer = issuer; return this; }
        public Builder secretKey(String secretKey) { this.secretKey = secretKey; return this; }
        public Builder algorithm(String algorithm) { this.algorithm = algorithm; return this; }
        public Builder accessTokenTtlSec(Integer accessTokenTtlSec) { this.accessTokenTtlSec = accessTokenTtlSec; return this; }
        public Builder refreshTokenTtlSec(Integer refreshTokenTtlSec) { this.refreshTokenTtlSec = refreshTokenTtlSec; return this; }
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }

        public JwtConfig build() {
            return new JwtConfig(this);
        }
    }
}