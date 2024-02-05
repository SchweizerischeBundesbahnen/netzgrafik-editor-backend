package ch.sbb.pfi.netzgrafikeditor.api.variant;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantCreateDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantCreateFromVersionDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VariantController {
    private final VariantService variantService;

    @PostMapping("/v1/projects/{projectId}/variants")
    public ResponseEntity<VariantId> createVariant(
            @PathVariable ProjectId projectId, @RequestBody VariantCreateDto createDto)
            throws NotFoundException, ForbiddenOperationException {
        val id = this.variantService.create(projectId, createDto);

        val uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .replacePath("/v1/variants/" + id.getValue())
                        .build()
                        .toUri();

        return ResponseEntity.created(uri).body(id);
    }

    @PostMapping("/v1/versions/{versionId}/variant/new")
    public ResponseEntity<VariantId> createVariantFromVersion(
            @PathVariable VersionId versionId, @RequestBody VariantCreateFromVersionDto createDto)
            throws NotFoundException, ForbiddenOperationException {
        val id = this.variantService.createFromVersion(versionId, createDto);

        val uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .replacePath("/v1/variants/" + id.getValue())
                        .build()
                        .toUri();

        return ResponseEntity.created(uri).body(id);
    }

    @DeleteMapping("/v1/variants/{variantId}/snapshots")
    public ResponseEntity<Void> dropSnapshots(@PathVariable VariantId variantId)
            throws NotFoundException {
        this.variantService.dropSnapshots(variantId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/variants/{variantId}")
    public VariantDto getVariant(@PathVariable VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        return this.variantService.getById(variantId);
    }

    @DeleteMapping("/v1/variants/{variantId}")
    public ResponseEntity deleteVariant(@PathVariable VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        this.variantService.delete(variantId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/v1/variants/{variantId}/archive")
    public ResponseEntity<Void> archiveVariant(@PathVariable VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        this.variantService.archive(variantId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/v1/variants/{variantId}/unarchive")
    public ResponseEntity<Void> unarchiveVariant(@PathVariable VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        this.variantService.unarchive(variantId);

        return ResponseEntity.noContent().build();
    }

    /**
     * This endpoint increase the published-version from existing snapshots, if they are in a
     * conflict with a already published version from another user. The new publish-version will be
     * the next unused.
     *
     * @param variantId Variant for which the published-version of the snapshots from the calling
     *     user should be increased
     * @return
     * @throws NotFoundException
     * @throws ForbiddenOperationException
     */
    @PutMapping("/v1/variants/{variantId}/snapshots/asNewest")
    public ResponseEntity<Void> raiseSnapshotsToNewestReleaseVersion(
            @PathVariable VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        variantService.raiseSnapshotsToNewestReleaseVersion(variantId);

        return ResponseEntity.ok().build();
    }
}
