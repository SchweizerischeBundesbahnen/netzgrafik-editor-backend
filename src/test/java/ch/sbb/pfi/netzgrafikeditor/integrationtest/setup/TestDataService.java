package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup;

import static ch.sbb.netzgrafikeditor.jooq.model.tables.Projects.PROJECTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.ProjectsUsers.PROJECTS_USERS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Variants.VARIANTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Versions.VERSIONS;

import lombok.RequiredArgsConstructor;

import org.jooq.DSLContext;
import org.jooq.TableRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TestDataService {

    private final DSLContext context;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertTestData(Collection<TableRecord>... records) {
        Arrays.stream(records).forEach(this::insertTestData);
    }

    public void insertTestData(Collection<TableRecord> records) {
        for (TableRecord record : records) {
            context.insertInto(record.getTable()).set(record).execute();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllData() {
        Stream.of(VERSIONS, VARIANTS, PROJECTS_USERS, PROJECTS)
                .forEach(table -> context.deleteFrom(table).execute());
    }
}
