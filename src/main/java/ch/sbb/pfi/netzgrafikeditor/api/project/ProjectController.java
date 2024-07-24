package ch.sbb.pfi.netzgrafikeditor.api.project;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectCreateUpdateDto;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectDto;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectSummaryDto;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;
import ch.sbb.pfi.netzgrafikeditor.common.ValidationErrorException;

import jakarta.validation.Valid;

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

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    private static Pattern USER_ID_PATTERN = Pattern.compile("^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$");

    @PostMapping("/v1/projects")
    public ResponseEntity<Long> createProject(@RequestBody ProjectCreateUpdateDto projectDto)
            throws ValidationErrorException {
        this.assertValidUserIds(projectDto);

        val id = this.projectService.create(projectDto);

        val uri =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .replacePath("/v1/projects/" + id.getValue())
                        .build()
                        .toUri();

        return ResponseEntity.created(uri).body(id.getValue());
    }

    @GetMapping("/v1/projects")
    public Collection<ProjectSummaryDto> getAllProjects() {
        return this.projectService.getAll();
    }

    @GetMapping("/v1/projects/{projectId}")
    public ProjectDto getProject(@PathVariable ProjectId projectId)
            throws NotFoundException, ForbiddenOperationException {
        return this.projectService.getById(projectId);
    }

    @DeleteMapping("/v1/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable ProjectId projectId)
            throws NotFoundException, ForbiddenOperationException {
        this.projectService.delete(projectId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/v1/projects/{id}")
    public ResponseEntity<Void> updateProject(
            @PathVariable ProjectId id, @Valid @RequestBody ProjectCreateUpdateDto projectDto)
            throws NotFoundException, ValidationErrorException, ForbiddenOperationException {
        this.assertValidUserIds(projectDto);
        this.projectService.update(id, projectDto);
        return ResponseEntity.noContent().build();
    }

    private void assertValidUserIds(ProjectCreateUpdateDto projectDto)
            throws ValidationErrorException {
        this.assertValidUserIds(projectDto.getReadUsers());
        this.assertValidUserIds(projectDto.getWriteUsers());
    }

    private void assertValidUserIds(Collection<String> userIds) throws ValidationErrorException {
        val invalidUserIds =
                userIds.stream()
                        .filter(id -> !USER_ID_PATTERN.matcher(id).matches())
                        .collect(Collectors.toList());

        if (!invalidUserIds.isEmpty()) {
            throw new ValidationErrorException(
                    "invalid user IDs: " + String.join(", ", invalidUserIds));
        }
    }

    @PutMapping("/v1/projects/{id}/archive")
    public ResponseEntity<Void> archiveProject(@PathVariable ProjectId id)
            throws NotFoundException, ForbiddenOperationException {
        this.projectService.archive(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/v1/projects/{id}/unarchive")
    public ResponseEntity<Void> unarchiveProject(@PathVariable ProjectId id)
            throws NotFoundException, ForbiddenOperationException {
        this.projectService.unarchive(id);

        return ResponseEntity.noContent().build();
    }
}
