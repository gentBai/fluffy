package com.halfhex.fluffy.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.halfhex.fluffy.mapper.*;
import io.vertx.core.Vertx;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

import javax.sql.DataSource;

public class MybatisPlusConfig {

    private final SqlSessionFactory sqlSessionFactory;

    public MybatisPlusConfig(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTransactionFactory(new SpringManagedTransactionFactory());

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        sessionFactory.setPlugins(interceptor);

        sqlSessionFactory = sessionFactory.getObject();
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public GatewayRouteMapper getGatewayRouteMapper() {
        return sqlSessionFactory.openSession().getMapper(GatewayRouteMapper.class);
    }

    public ServiceInstanceMapper getServiceInstanceMapper() {
        return sqlSessionFactory.openSession().getMapper(ServiceInstanceMapper.class);
    }

    public GatewayServiceMapper getGatewayServiceMapper() {
        return sqlSessionFactory.openSession().getMapper(GatewayServiceMapper.class);
    }

    public BlacklistMapper getBlacklistMapper() {
        return sqlSessionFactory.openSession().getMapper(BlacklistMapper.class);
    }

    public WhitelistMapper getWhitelistMapper() {
        return sqlSessionFactory.openSession().getMapper(WhitelistMapper.class);
    }

    public RateLimitRuleMapper getRateLimitRuleMapper() {
        return sqlSessionFactory.openSession().getMapper(RateLimitRuleMapper.class);
    }

    public ApiKeyMapper getApiKeyMapper() {
        return sqlSessionFactory.openSession().getMapper(ApiKeyMapper.class);
    }

    public CircuitBreakerStateMapper getCircuitBreakerStateMapper() {
        return sqlSessionFactory.openSession().getMapper(CircuitBreakerStateMapper.class);
    }

    public LoadBalanceStrategyMapper getLoadBalanceStrategyMapper() {
        return sqlSessionFactory.openSession().getMapper(LoadBalanceStrategyMapper.class);
    }
}