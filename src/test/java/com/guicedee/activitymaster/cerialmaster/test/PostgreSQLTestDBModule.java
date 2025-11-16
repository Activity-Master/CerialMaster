package com.guicedee.activitymaster.cerialmaster.test;

import com.guicedee.activitymaster.fsdm.db.ActivityMasterDBModule;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.vertxpersistence.ConnectionBaseInfo;
import com.guicedee.vertxpersistence.DatabaseModule;
import com.guicedee.vertxpersistence.annotations.EntityManager;
import com.guicedee.vertxpersistence.implementations.postgres.PostgresConnectionBaseInfo;
import jakarta.validation.constraints.NotNull;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@EntityManager(value = "ActivityMaster-Test", defaultEm = true)
public class PostgreSQLTestDBModule
        extends DatabaseModule<PostgreSQLTestDBModule>
        implements IGuiceModule<PostgreSQLTestDBModule>
{
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("fsdm")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        ActivityMasterDBModule.forTests = true;
        postgresContainer.start();
         try {
        // Copy and execute postgres_fsdm.sql
        Path fsdmSqlPath = Paths.get("src/test/resources/postgres_fsdm.sql");

        postgresContainer.copyFileToContainer(
            MountableFile.forHostPath(fsdmSqlPath),
            "/docker-entrypoint-initdb.d/init_fsdm.sql"
        );

        postgresContainer.copyFileToContainer(
            MountableFile.forHostPath(fsdmSqlPath),
            "/tmp/init_fsdm.sql"
        );

        Container.ExecResult fsdmResult = postgresContainer.execInContainer(
            "psql",
            "-U", postgresContainer.getUsername(),
            "-d", postgresContainer.getDatabaseName(),
            "-f", "/tmp/init_fsdm.sql"
        );

        if (fsdmResult.getExitCode() != 0) {
            System.err.println("STDERR: " + fsdmResult.getStderr());
            throw new RuntimeException("psql fsdm script execution failed: " + fsdmResult.getStderr());
        }

        System.out.println("✅ DB FSDM Script Executed Successfully:\n" + fsdmResult.getStdout());
        
        // Copy and execute postgres_structure.sql
        Path structureSqlPath = Paths.get("src/test/resources/postgres_structure.sql");

        postgresContainer.copyFileToContainer(
            MountableFile.forHostPath(structureSqlPath),
            "/docker-entrypoint-initdb.d/init_structure.sql"
        );

        postgresContainer.copyFileToContainer(
            MountableFile.forHostPath(structureSqlPath),
            "/tmp/init_structure.sql"
        );

        Container.ExecResult structureResult = postgresContainer.execInContainer(
            "psql",
            "-U", postgresContainer.getUsername(),
            "-d", postgresContainer.getDatabaseName(),
            "-f", "/tmp/init_structure.sql"
        );

        if (structureResult.getExitCode() != 0) {
            System.err.println("STDERR: " + structureResult.getStderr());
            throw new RuntimeException("psql structure script execution failed: " + structureResult.getStderr());
        }

        System.out.println("✅ DB Structure Script Executed Successfully:\n" + structureResult.getStdout());

    } catch (Exception e) {
        throw new RuntimeException("Failed to execute SQL initialization scripts", e);
    }
    }

    @NotNull
    @Override
    protected String getPersistenceUnitName()
    {
        return "ActivityMaster-Test";
    }

    @Override
    @NotNull
    protected ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnitDescriptor unit, Properties filteredProperties)
    {
        PostgresConnectionBaseInfo connectionInfo = new PostgresConnectionBaseInfo();
        connectionInfo.setServerName(postgresContainer.getHost());
        connectionInfo.setPort(String.valueOf(postgresContainer.getFirstMappedPort()));
        connectionInfo.setDatabaseName(postgresContainer.getDatabaseName());
        connectionInfo.setUsername(postgresContainer.getUsername());
        connectionInfo.setPassword(postgresContainer.getPassword());
        connectionInfo.setDefaultConnection(true);
        connectionInfo.setReactive(true);
        return connectionInfo;
    }

    @NotNull
    @Override
    protected String getJndiMapping()
    {
        return "jdbc:activitymaster-test";
    }


    @Override
    public Integer sortOrder()
    {
        return 10;
    }
}
