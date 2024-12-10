package spribe.test.task.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class DbConfig {
    @Bean
    public DataSource dataSource(final DataSourceProperties dataSourceProperties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
        hikariConfig.setUsername(dataSourceProperties.getUsername());
        hikariConfig.setPassword(dataSourceProperties.getPassword());
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setAutoCommit(false);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public SpringLiquibase liquibase(final LiquibaseProperties liquibaseProperties,
                                     final DataSource dataSource) {
        SpringLiquibase springLiquibase  = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog(liquibaseProperties.getChangeLog());
        springLiquibase.setLiquibaseSchema(liquibaseProperties.getDefaultSchema());
        springLiquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        springLiquibase.setShouldRun(liquibaseProperties.isEnabled());

        return springLiquibase;
    }
}
