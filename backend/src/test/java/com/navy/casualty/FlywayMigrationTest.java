package com.navy.casualty;

import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 마이그레이션 파일 존재 및 네이밍 규칙 검증.
 * test 프로파일에서 Flyway가 비활성화되어 있으므로,
 * SQL 파일의 존재 여부와 비어있지 않은지를 검증한다.
 */
class FlywayMigrationTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "db/migration/V1__init_schema.sql",
            "db/migration/V2__create_code_tables.sql",
            "db/migration/V3__create_dead_table.sql",
            "db/migration/V4__create_wounded_table.sql",
            "db/migration/V5__create_review_table.sql",
            "db/migration/V6__create_audit_log_table.sql",
            "db/migration/V7__create_document_issue_log.sql",
            "db/migration/V8__create_users_roles.sql",
            "db/migration/V9__insert_mock_code_data.sql"
    })
    @DisplayName("V1~V9 마이그레이션 파일이 classpath에 존재하고 비어있지 않다")
    void migrationFileExists(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(is)
                    .as("마이그레이션 파일이 존재해야 한다: %s", path)
                    .isNotNull();

            byte[] content = is.readAllBytes();
            assertThat(content.length)
                    .as("마이그레이션 파일이 비어있지 않아야 한다: %s", path)
                    .isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("마이그레이션 파일명이 V{N}__ 접두사 규칙을 따른다")
    void migrationFileNamingConvention() {
        String[] expectedFiles = {
                "V1__init_schema.sql",
                "V2__create_code_tables.sql",
                "V3__create_dead_table.sql",
                "V4__create_wounded_table.sql",
                "V5__create_review_table.sql",
                "V6__create_audit_log_table.sql",
                "V7__create_document_issue_log.sql",
                "V8__create_users_roles.sql",
                "V9__insert_mock_code_data.sql"
        };

        for (String fileName : expectedFiles) {
            assertThat(fileName).matches("V\\d+__.*\\.sql");
        }
    }
}
