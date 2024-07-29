package ch.sbb.pfi.netzgrafikeditor.api.common;

import static ch.sbb.netzgrafikeditor.jooq.model.tables.Projects.PROJECTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.ProjectsUsers.PROJECTS_USERS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Variants.VARIANTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Versions.VERSIONS;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;
import ch.sbb.pfi.netzgrafikeditor.common.util.CastHelper;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

import org.jooq.DSLContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    // See: https://openid.net/specs/openid-connect-core-1_0.html#Claims
    private static final String USER_ID_FROM_EMAIL_CLAIM = "email";
    private static final String Subject_Identifier_CLAIM = "sub";
    private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

    private final DSLContext context;

    public String getCurrentUserEmail() {
        return this.tryGetClaim(PREFERRED_USERNAME_CLAIM)
                .orElseThrow(() -> new BadCredentialsException("E-Mail missing in token"));
    }

    public UserId getCurrentUserIdFromEmail() {
        val idString =
                this.tryGetClaim(USER_ID_FROM_EMAIL_CLAIM)
                        .orElseThrow(() -> new BadCredentialsException("User ID missing in token"));

        return UserId.of(idString);
    }

    public UserId getCurrentSubjectId() {
        val idString =
            this.tryGetClaim(Subject_Identifier_CLAIM)
                .orElseThrow(() -> new BadCredentialsException("Sub identifier missing in token"));

        return UserId.of(idString);
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(SecurityConfig.ADMIN_ROLE::equals);
    }

    private Optional<String> tryGetClaim(String claim) {
        return CastHelper.tryCast(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                        Jwt.class)
                .flatMap(
                        authToken ->
                                CastHelper.tryCast(authToken.getClaims().get(claim), String.class));
    }

    public AuthorizationInfo getAuthorizationInfo(ProjectId projectId) throws NotFoundException {
        return this.context
                .select(PROJECTS.IS_ARCHIVED, PROJECTS_USERS.IS_EDITOR)
                .from(PROJECTS)
                .leftJoin(PROJECTS_USERS)
                .on(
                        PROJECTS_USERS.PROJECT_ID.eq(PROJECTS.ID))
                .where(PROJECTS.ID.eq(projectId.getValue()).and(
                    PROJECTS_USERS.USER_ID.eq(this.getCurrentUserIdFromEmail().getValue()).or(
                        PROJECTS_USERS.USER_ID.eq(this.getCurrentSubjectId().getValue())
                    )))
                .fetchOptional()
                .map(
                        record -> {
                            val isArchived = record.getValue(PROJECTS.IS_ARCHIVED);
                            val isEditor =
                                    Optional.ofNullable(record.getValue(PROJECTS_USERS.IS_EDITOR));
                            return this.getAuthorizationInfo(isArchived, isEditor, this.isAdmin());
                        })
                .orElseThrow(NotFoundException.of("projects", projectId));
    }

    public AuthorizationInfo getAuthorizationInfo(VariantId variantId) throws NotFoundException {
        return this.context
                .select(PROJECTS.IS_ARCHIVED, VARIANTS.IS_ARCHIVED, PROJECTS_USERS.IS_EDITOR)
                .from(PROJECTS)
                .join(VARIANTS)
                .on(VARIANTS.PROJECT_ID.eq(PROJECTS.ID))
                .leftJoin(PROJECTS_USERS)
                .on(
                    PROJECTS_USERS.PROJECT_ID.eq(PROJECTS.ID))
                .where(PROJECTS.ID.eq(projectId.getValue()).and(
                    PROJECTS_USERS.USER_ID.eq(this.getCurrentUserIdFromEmail().getValue()).or(
                            PROJECTS_USERS.USER_ID.eq(this.getCurrentSubjectId().getValue())
                        )))
                .fetchOptional()
                .map(
                        record -> {
                            val isProjectArchived = record.getValue(PROJECTS.IS_ARCHIVED);
                            val isVariantArchived = record.getValue(VARIANTS.IS_ARCHIVED);
                            val isEditor =
                                    Optional.ofNullable(record.getValue(PROJECTS_USERS.IS_EDITOR));
                            return this.getAuthorizationInfo(
                                    isProjectArchived || isVariantArchived,
                                    isEditor,
                                    this.isAdmin());
                        })
                .orElseThrow(NotFoundException.of("variants", variantId));
    }

    public AuthorizationInfo getAuthorizationInfo(VersionId versionId) throws NotFoundException {
        return this.context
                .select(PROJECTS.IS_ARCHIVED, VARIANTS.IS_ARCHIVED, PROJECTS_USERS.IS_EDITOR)
                .from(PROJECTS)
                .join(VARIANTS)
                .on(VARIANTS.PROJECT_ID.eq(PROJECTS.ID))
                .join(VERSIONS)
                .on(VERSIONS.VARIANT_ID.eq(VARIANTS.ID))
                .leftJoin(PROJECTS_USERS)
                .on(
                    PROJECTS_USERS.PROJECT_ID.eq(PROJECTS.ID))
                .where(PROJECTS.ID.eq(projectId.getValue()).and(
                    PROJECTS_USERS.USER_ID.eq(this.getCurrentUserIdFromEmail().getValue()).or(
                            PROJECTS_USERS.USER_ID.eq(this.getCurrentSubjectId().getValue())
                        )))
                .fetchOptional()
                .map(
                        record -> {
                            val isProjectArchived = record.getValue(PROJECTS.IS_ARCHIVED);
                            val isVariantArchived = record.getValue(PROJECTS.IS_ARCHIVED);
                            val isEditor =
                                    Optional.ofNullable(record.getValue(PROJECTS_USERS.IS_EDITOR));
                            return this.getAuthorizationInfo(
                                    isProjectArchived || isVariantArchived,
                                    isEditor,
                                    this.isAdmin());
                        })
                .orElseThrow(NotFoundException.of("versions", versionId));
    }

    private AuthorizationInfo getAuthorizationInfo(
            boolean isArchived, Optional<Boolean> isEditor, boolean isAdmin) {
        val isReadable = isAdmin || isEditor.isPresent();
        val isWritable = !isArchived && (isAdmin || isEditor.orElse(false));
        val isDeletable = isArchived && (isAdmin || isEditor.orElse(false));
        return new AuthorizationInfo(isReadable, isWritable, isDeletable);
    }

    @Value
    @Builder
    public static class AuthorizationInfo {
        @NonNull Boolean readable;

        @NonNull Boolean writable;

        @NonNull Boolean deletable;

        public void assertReadable() throws ForbiddenOperationException {
            if (!readable) {
                throw new ForbiddenOperationException("entity not readable");
            }
        }

        public void assertWritable() throws ForbiddenOperationException {
            if (!writable) {
                throw new ForbiddenOperationException("entity not writable");
            }
        }

        public void assertDeletable() throws ForbiddenOperationException {
            if (!deletable) {
                throw new ForbiddenOperationException("entity not deletable");
            }
        }
    }
}
