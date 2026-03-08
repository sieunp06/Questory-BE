package com.ssafy.questory.support;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@TestConfiguration
public class QueryCountConfig {

    @Bean(name = "originalDataSource")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource originalDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSource originalDataSource) {
        return ProxyDataSourceBuilder
                .create(originalDataSource)
                .name("DS-PROXY")
                .countQuery()
                .build();
    }
}
