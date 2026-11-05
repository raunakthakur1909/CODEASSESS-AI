package com.codeassess.backend.config;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    private final DataSource dataSource;
    private final boolean enabled;

    public FlywayConfig(DataSource dataSource, @Value("${spring.flyway.enabled:true}") boolean enabled) {
        this.dataSource = dataSource;
        this.enabled = enabled;
    }

    @PostConstruct
    public void migrate() {
        if (!enabled) {
            logger.info("Flyway migrations are disabled");
            return;
        }

        String baselineVersion = "1";
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .baselineVersion(baselineVersion)
                .locations("classpath:db/migration")
                .load();
                
        flyway.migrate();
        logger.info("Flyway migrations completed. Current schema version: {}", flyway.info().current().getVersion());
    }
}
