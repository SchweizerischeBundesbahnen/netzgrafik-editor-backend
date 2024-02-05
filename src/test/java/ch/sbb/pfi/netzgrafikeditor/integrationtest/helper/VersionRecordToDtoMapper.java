package ch.sbb.pfi.netzgrafikeditor.integrationtest.helper;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionRecordToDtoMapper {
    public static VersionDto map(VersionsRecord record) {
        return VersionDto.builder()
                .id(VersionId.of(record.getId()))
                .variantId(VariantId.of(record.getVariantId()))
                .releaseVersion(record.getReleaseVersion())
                .snapshotVersion(Optional.ofNullable(record.getSnapshotVersion()))
                .name(record.getName())
                .comment(record.getComment())
                .createdBy(UserId.of(record.getCreatedBy()))
                .createdAt(record.getCreatedAt())
                .build();
    }
}
