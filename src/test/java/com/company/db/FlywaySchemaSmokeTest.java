package com.company.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FlywaySchemaSmokeTest extends AbstractFlywayIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void allFlywayMigrationsApplySuccessfully() {
        var info = flyway.info();

        assertThat(info.applied()).isNotEmpty();
        assertThat(info.pending()).isEmpty();
        var current = info.current();
        assertThat(current).isNotNull();
        assertThat(current.getState().isApplied()).isTrue();
    }
}
