package com.seguradora.hibrida.projection.versioning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ProjectionVersion}.
 */
@DisplayName("ProjectionVersion Tests")
class ProjectionVersionTest {

    // =========================================================================
    // Construção via builder
    // =========================================================================

    @Nested
    @DisplayName("Construção via builder")
    class ConstrucaoViaBuilder {

        @Test
        @DisplayName("Builder deve criar instância com campos corretos")
        void builderShouldCreateInstanceWithCorrectFields() {
            ProjectionVersion version = ProjectionVersion.builder()
                    .projectionName("TestProjection")
                    .version(1)
                    .schemaHash("abc123")
                    .description("First version")
                    .createdAt(Instant.now())
                    .build();

            assertThat(version.getProjectionName()).isEqualTo("TestProjection");
            assertThat(version.getVersion()).isEqualTo(1);
            assertThat(version.getSchemaHash()).isEqualTo("abc123");
            assertThat(version.getDescription()).isEqualTo("First version");
        }

        @Test
        @DisplayName("Builder deve usar 'SYSTEM' como createdBy padrão")
        void builderShouldUseSystemAsDefaultCreatedBy() {
            ProjectionVersion version = ProjectionVersion.builder()
                    .projectionName("P")
                    .version(1)
                    .schemaHash("hash")
                    .createdAt(Instant.now())
                    .build();

            assertThat(version.getCreatedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("Builder deve usar PENDING como migrationStatus padrão")
        void builderShouldUsePendingAsDefaultMigrationStatus() {
            ProjectionVersion version = ProjectionVersion.builder()
                    .projectionName("P")
                    .version(1)
                    .schemaHash("hash")
                    .createdAt(Instant.now())
                    .build();

            assertThat(version.getMigrationStatus())
                    .isEqualTo(ProjectionVersion.MigrationStatus.PENDING);
        }

        @Test
        @DisplayName("Builder deve usar requiresRebuild=true por padrão")
        void builderShouldUseRequiresRebuildTrueByDefault() {
            ProjectionVersion version = ProjectionVersion.builder()
                    .projectionName("P")
                    .version(1)
                    .schemaHash("hash")
                    .createdAt(Instant.now())
                    .build();

            assertThat(version.getRequiresRebuild()).isTrue();
        }
    }

    // =========================================================================
    // Ciclo de vida de migração
    // =========================================================================

    @Nested
    @DisplayName("Ciclo de vida de migração")
    class CicloVidaMigracao {

        private ProjectionVersion buildVersion() {
            return ProjectionVersion.builder()
                    .projectionName("TestProjection")
                    .version(1)
                    .schemaHash("hash")
                    .createdAt(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("startMigration() deve definir status como IN_PROGRESS")
        void startMigrationShouldSetStatusToInProgress() {
            ProjectionVersion version = buildVersion();
            version.startMigration();

            assertThat(version.getMigrationStatus())
                    .isEqualTo(ProjectionVersion.MigrationStatus.IN_PROGRESS);
            assertThat(version.getMigrationStartedAt()).isNotNull();
            assertThat(version.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("completeMigration() deve definir status como COMPLETED")
        void completeMigrationShouldSetStatusToCompleted() {
            ProjectionVersion version = buildVersion();
            version.startMigration();
            version.completeMigration();

            assertThat(version.getMigrationStatus())
                    .isEqualTo(ProjectionVersion.MigrationStatus.COMPLETED);
            assertThat(version.getMigrationCompletedAt()).isNotNull();
            assertThat(version.getRequiresRebuild()).isFalse();
            assertThat(version.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("failMigration() deve definir status como FAILED")
        void failMigrationShouldSetStatusToFailed() {
            ProjectionVersion version = buildVersion();
            version.startMigration();
            version.failMigration("DB error");

            assertThat(version.getMigrationStatus())
                    .isEqualTo(ProjectionVersion.MigrationStatus.FAILED);
            assertThat(version.getMigrationError()).isEqualTo("DB error");
            assertThat(version.isFailed()).isTrue();
        }
    }

    // =========================================================================
    // getProgress
    // =========================================================================

    @Nested
    @DisplayName("getProgress()")
    class GetProgress {

        @Test
        @DisplayName("Deve retornar 0 quando eventsToProcess é 0")
        void shouldReturnZeroWhenEventsToProcessIsZero() {
            ProjectionVersion version = ProjectionVersion.builder()
                    .projectionName("P")
                    .version(1)
                    .schemaHash("hash")
                    .createdAt(Instant.now())
                    .build();

            assertThat(version.getProgress()).isZero();
        }

        @Test
        @DisplayName("Deve calcular progresso corretamente")
        void shouldCalculateProgressCorrectly() {
            ProjectionVersion version = ProjectionVersion.builder()
                    .projectionName("P")
                    .version(1)
                    .schemaHash("hash")
                    .createdAt(Instant.now())
                    .eventsToProcess(100L)
                    .eventsProcessed(50L)
                    .build();

            assertThat(version.getProgress()).isEqualTo(50.0);
        }
    }

    // =========================================================================
    // Chave composta
    // =========================================================================

    @Test
    @DisplayName("ProjectionVersionId deve ser serializável com campos corretos")
    void projectionVersionIdShouldBeSerializableWithCorrectFields() {
        ProjectionVersion.ProjectionVersionId id =
                new ProjectionVersion.ProjectionVersionId("MyProjection", 2);

        assertThat(id.getProjectionName()).isEqualTo("MyProjection");
        assertThat(id.getVersion()).isEqualTo(2);
    }

    // =========================================================================
    // MigrationStatus enum
    // =========================================================================

    @Test
    @DisplayName("MigrationStatus deve conter os valores esperados")
    void migrationStatusShouldContainExpectedValues() {
        assertThat(ProjectionVersion.MigrationStatus.values())
                .containsExactlyInAnyOrder(
                        ProjectionVersion.MigrationStatus.PENDING,
                        ProjectionVersion.MigrationStatus.IN_PROGRESS,
                        ProjectionVersion.MigrationStatus.COMPLETED,
                        ProjectionVersion.MigrationStatus.FAILED
                );
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString() deve retornar String não nula")
    void toStringShouldReturnNonNull() {
        ProjectionVersion version = ProjectionVersion.builder()
                .projectionName("TestProjection")
                .version(1)
                .schemaHash("hash")
                .createdAt(Instant.now())
                .build();

        assertThat(version.toString()).isNotNull().isNotBlank().contains("TestProjection");
    }
}
