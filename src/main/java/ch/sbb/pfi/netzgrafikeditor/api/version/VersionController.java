package ch.sbb.pfi.netzgrafikeditor.api.version;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionCreateReleaseDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionCreateSnapshotDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.common.ConflictException;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    /**
     * Create new snapshot version.
     *
     * @param baseVersionId base version the new snapshot is extending on
     * @param createSnapshotDto dto containing relevant payload
     * @return
     * @throws NotFoundException
     */
    @PostMapping("/v1/versions/{baseVersionId}/snapshot")
    public ResponseEntity<VersionId> createSnapshotVersion(
            @PathVariable VersionId baseVersionId,
            @RequestBody VersionCreateSnapshotDto createSnapshotDto)
            throws NotFoundException, ConflictException, ForbiddenOperationException {
        val id = versionService.createSnapshotVersion(baseVersionId, createSnapshotDto);

        val uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .replacePath("/v1/versions/" + id.getValue())
                        .build()
                        .toUri();

        return ResponseEntity.created(uri).body(id);
    }

    /**
     * Create new release version
     *
     * @param snapshotVersionId base snapshot version that is used to create the release version on
     * @param createReleaseDto dto containing relevant payload
     * @return
     * @throws NotFoundException
     */
    @PostMapping("/v1/versions/{snapshotVersionId}/release")
    public ResponseEntity<VersionId> createReleaseVersion(
            @PathVariable VersionId snapshotVersionId,
            @RequestBody VersionCreateReleaseDto createReleaseDto)
            throws NotFoundException, ConflictException, ForbiddenOperationException {
        val id = versionService.createReleaseVersion(snapshotVersionId, createReleaseDto);

        val uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .replacePath("/v1/versions/" + id.getValue())
                        .build()
                        .toUri();

        return ResponseEntity.created(uri).body(id);
    }

    @PostMapping("/v1/versions/{versionId}/restore")
    public ResponseEntity<VersionId> restoreVersion(@PathVariable VersionId versionId)
            throws NotFoundException, ConflictException, ForbiddenOperationException {
        val id = versionService.restore(versionId);

        val uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .replacePath("/v1/versions/" + id.getValue())
                        .build()
                        .toUri();

        return ResponseEntity.created(uri).body(id);
    }

    @GetMapping("/v1/versions/{versionId}")
    public VersionDto getVersion(@PathVariable VersionId versionId)
            throws NotFoundException, ForbiddenOperationException {
        return this.versionService.getVersion(versionId);
    }

    @GetMapping(value = "/v1/versions/{versionId}/model")
    public JsonNode getVersionModel(@PathVariable VersionId versionId)
            throws NotFoundException, JsonProcessingException {
        return this.versionService.getVersionModel(versionId);
    }
}
