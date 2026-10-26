package com.studentgradetracker.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    /**
     * Supports standard cloud-native DATABASE_URL environment variables (e.g. Render, Railway, Heroku)
     * which inject postgres://username:password@host:port/database.
     * When present and in postgres:// format, converts to jdbc:postgresql://.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource cloudPostgresDataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return null;
        }

        try {
            log.info("Detected cloud DATABASE_URL environment variable. Configuring PostgreSQL DataSource...");
            HikariConfig config = new HikariConfig();

            if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
                URI uri = new URI(databaseUrl);
                String userInfo = uri.getUserInfo();
                String username = "";
                String password = "";
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String host = uri.getHost();
                String path = uri.getPath(); // includes leading '/'
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
            } else {
                config.setJdbcUrl(databaseUrl);
            }

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setPoolName("ProductionHikariPool");

            log.info("Successfully configured production DataSource for {}", config.getJdbcUrl());
            return new HikariDataSource(config);
        } catch (Exception e) {
            log.error("Failed to parse DATABASE_URL: {}. Falling back to default Spring Boot datasource configuration.", e.getMessage());
            throw new IllegalStateException("Invalid DATABASE_URL configuration", e);
        }
    }
}
