package com.skateboard.podcast.feed.service.events.application.adapter.in.rest;

import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.feed.service.events.application.dto.ImportEventCommand;
import com.skateboard.podcast.feed.service.events.application.port.in.AdminFeedEventsUseCase;
import com.skateboard.podcast.standardbe.api.AdminEventsApi;
import com.skateboard.podcast.standardbe.api.model.CreateEventRequest;
import com.skateboard.podcast.standardbe.api.model.EventDetails;
import com.skateboard.podcast.standardbe.api.model.EventStatus;
import com.skateboard.podcast.standardbe.api.model.EventSummary;
import com.skateboard.podcast.standardbe.api.model.ImportEventsRequest;
import com.skateboard.podcast.standardbe.api.model.PageEventSummary;
import com.skateboard.podcast.standardbe.api.model.UpdateEventRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class AdminEventsController implements AdminEventsApi {

    private final AdminFeedEventsUseCase adminFeedEventsService;
    private final EventApiMapper eventApiMapper;

    public AdminEventsController(
            final AdminFeedEventsUseCase adminFeedEventsService,
            final EventApiMapper eventApiMapper
    ) {
        this.adminFeedEventsService = adminFeedEventsService;
        this.eventApiMapper = eventApiMapper;
    }

    @Override
    @PostMapping(
            value = AdminEventsApi.PATH_ADMIN_EVENT_CREATE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EventDetails> adminEventCreate(final CreateEventRequest createEventRequest) {
        final UUID createdBy = requireUserId();
        final var created = adminFeedEventsService.createDraft(
                createEventRequest.getTitle(),
                createEventRequest.getSlug(),
                createEventRequest.getExcerpt(),
                createEventRequest.getTags(),
                eventApiMapper.writeThumbnailJson(createEventRequest.getThumbnail()),
                eventApiMapper.writeContentJson(createEventRequest.getContent()),
                toInstant(createEventRequest.getStartAt()),
                toInstant(createEventRequest.getEndAt()),
                createEventRequest.getTimezone(),
                createEventRequest.getLocation(),
                createEventRequest.getTicketsUrl(),
                createdBy
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(eventApiMapper.toEventDetails(created));
    }

    @Override
    @GetMapping(
            value = AdminEventsApi.PATH_ADMIN_EVENT_CREATE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageEventSummary> adminEventList(
            final Integer page,
            final Integer size,
            final EventStatus status
    ) {
        final int safePage = page == null ? 0 : page;
        final int safeSize = size == null ? 20 : size;
        final var domainStatus = status == null
                ? null
                : com.skateboard.podcast.domain.valueobject.EventStatus.from(status.name());
        final var items = adminFeedEventsService.list(safePage, safeSize, domainStatus);
        return ResponseEntity.ok(eventApiMapper.toPageEventSummary(items, safePage, safeSize));
    }

    @Override
    @GetMapping(
            value = AdminEventsApi.PATH_ADMIN_EVENT_UPDATE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EventDetails> adminEventGet(final UUID id) {
        return adminFeedEventsService.getById(id)
                .map(details -> ResponseEntity.ok(eventApiMapper.toEventDetails(details)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @PutMapping(
            value = AdminEventsApi.PATH_ADMIN_EVENT_UPDATE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EventDetails> adminEventUpdate(final UUID id, final UpdateEventRequest updateEventRequest) {
        final var updated = adminFeedEventsService.updateById(
                id,
                updateEventRequest.getTitle(),
                updateEventRequest.getSlug(),
                updateEventRequest.getExcerpt(),
                updateEventRequest.getTags(),
                eventApiMapper.writeThumbnailJson(updateEventRequest.getThumbnail()),
                eventApiMapper.writeContentJson(updateEventRequest.getContent()),
                toInstant(updateEventRequest.getStartAt()),
                toInstant(updateEventRequest.getEndAt()),
                updateEventRequest.getTimezone(),
                updateEventRequest.getLocation(),
                updateEventRequest.getTicketsUrl()
        );
        return ResponseEntity.ok(eventApiMapper.toEventDetails(updated));
    }

    @Override
    @PostMapping(
            value = AdminEventsApi.PATH_ADMIN_EVENT_PUBLISH,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EventDetails> adminEventPublish(final UUID id) {
        final var updated = adminFeedEventsService.publishById(id);
        return ResponseEntity.ok(eventApiMapper.toEventDetails(updated));
    }

    @Override
    @DeleteMapping(value = AdminEventsApi.PATH_ADMIN_EVENT_UPDATE)
    public ResponseEntity<Void> adminEventDelete(final UUID id) {
        adminFeedEventsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(
            value = "/admin/events/import",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<EventSummary>> adminEventImport(final ImportEventsRequest importEventsRequest) {
        final UUID createdBy = requireUserId();
        final List<ImportEventCommand> commands = toImportCommands(importEventsRequest);
        final var imported = adminFeedEventsService.importEvents(commands, createdBy);
        final List<EventSummary> response = imported.stream()
                .map(eventApiMapper::toEventSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping(value = "/admin/events/reset")
    public ResponseEntity<Void> adminEventReset() {
        adminFeedEventsService.resetAll();
        return ResponseEntity.noContent().build();
    }

    private List<ImportEventCommand> toImportCommands(final ImportEventsRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return List.of();
        }
        final List<ImportEventCommand> commands = new ArrayList<>(request.getItems().size());
        for (final CreateEventRequest item : request.getItems()) {
            commands.add(new ImportEventCommand(
                    item.getTitle(),
                    item.getSlug(),
                    item.getExcerpt(),
                    item.getTags(),
                    eventApiMapper.writeThumbnailJson(item.getThumbnail()),
                    eventApiMapper.writeContentJson(item.getContent()),
                    toInstant(item.getStartAt()),
                    toInstant(item.getEndAt()),
                    item.getTimezone(),
                    item.getLocation(),
                    item.getTicketsUrl()
            ));
        }
        return commands;
    }

    private static Instant toInstant(final OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static UUID requireUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        if (auth.getPrincipal() instanceof final CurrentUser currentUser) {
            return currentUser.userId();
        }
        throw new IllegalStateException("No authenticated user");
    }
}


