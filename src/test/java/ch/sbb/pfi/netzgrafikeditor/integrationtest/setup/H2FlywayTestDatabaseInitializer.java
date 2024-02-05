package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup;

import lombok.extern.slf4j.Slf4j;

import org.jooq.meta.extensions.ddl.DDLDatabase;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

@Component
@Slf4j
public class H2FlywayTestDatabaseInitializer {

    public H2FlywayTestDatabaseInitializer(DataSource dataSource) throws Exception {
        log.info("Initializing H2 Test-Database Schema");
        var ddlDatabase = new DrilledDDLDatabase();
        try (Connection connection = dataSource.getConnection()) {
            ddlDatabase.setConnectionInternal(connection);
            ddlDatabase.setBasedir(
                    FileSystems.getDefault().getPath("").toAbsolutePath().toString());
            ddlDatabase.setProperties(new Properties());
            ddlDatabase.getProperties().setProperty("scripts", "src/main/resources/db/migration");
            ddlDatabase.getProperties().setProperty("sort", "flyway");
            ddlDatabase.export();
            log.info("Successfully initialized H2 Test-Database Schema");
        } finally {
            ddlDatabase.close();
        }
    }

    /** DDLDatabase has to be extended to make protected {@link DDLDatabase#export()} accessible */
    private static class DrilledDDLDatabase extends DDLDatabase {

        public void export() throws Exception {
            super.export();
        }

        /**
         * The relevant 'connection' field is a private attribute in AbstractInterpretingDatabase
         * and has to be accessed using reflection
         *
         * @param connection
         * @throws IllegalAccessException
         * @throws NoSuchFieldException
         */
        public void setConnectionInternal(Connection connection)
                throws IllegalAccessException, NoSuchFieldException {
            Field connectionField =
                    getClass().getSuperclass().getSuperclass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(this, connection);
        }
    }
}
