package com.skateboard.podcast.appconfig.service.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.appconfig.service.application.dto.NavigationConfigView;
import com.skateboard.podcast.appconfig.service.application.dto.NavigationTabView;
import com.skateboard.podcast.appconfig.service.application.port.in.NavigationConfigUseCase;
import com.skateboard.podcast.appconfig.service.application.port.out.NavigationConfigEventPublisher;
import com.skateboard.podcast.appconfig.service.application.port.out.NavigationConfigRepository;
import com.skateboard.podcast.domain.exception.ValidationException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NavigationConfigService implements NavigationConfigUseCase {

    private static final long CONFIG_ID = 1L;
    private static final String SETTINGS_TAB_ID = "settings";
    private static final String INDEX_TAB_ID = "index";

    private final NavigationConfigRepository repository;
    private final NavigationConfigEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public NavigationConfigService(
            final NavigationConfigRepository repository,
            final NavigationConfigEventPublisher eventPublisher,
            final ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public NavigationConfigView get() {
        return toView(loadOrCreate());
    }

    @Override
    public NavigationConfigView update(final NavigationConfigView updated) {
        validate(updated);
        final NavigationConfigRepository.NavigationConfigRecord existing = loadOrCreate();
        final NavigationConfigRepository.NavigationConfigRecord saved = repository.save(
                toRecord(existing.id(), updated, existing.updatedAt())
        );
        eventPublisher.publishNavigationUpdated(Instant.now());
        return toView(saved);
    }

    private NavigationConfigRepository.NavigationConfigRecord loadOrCreate() {
        return repository.findById(CONFIG_ID)
                .orElseGet(this::createDefault);
    }

    private NavigationConfigRepository.NavigationConfigRecord createDefault() {
        final NavigationConfigView view = defaultConfig();
        final NavigationConfigRepository.NavigationConfigRecord record = toRecord(CONFIG_ID, view, null);
        return repository.save(record);
    }

    private NavigationConfigView defaultConfig() {
        final NavigationTabView events = new NavigationTabView(
                "index",
                "Events",
                "Calendar",
                0,
                true,
                true
        );
        final NavigationTabView settings = new NavigationTabView(
                SETTINGS_TAB_ID,
                "Settings",
                "Settings",
                1,
                true,
                true
        );
        return new NavigationConfigView(List.of(events, settings));
    }

    private void validate(final NavigationConfigView config) {
        if (config == null) {
            throw new ValidationException("navigation config cannot be null");
        }
        if (config.tabs() == null || config.tabs().isEmpty()) {
            throw new ValidationException("tabs cannot be empty");
        }

        final Set<String> ids = new HashSet<>();
        boolean settingsFound = false;
        boolean indexFound = false;

        for (final NavigationTabView tab : config.tabs()) {
            if (tab == null) {
                throw new ValidationException("tab cannot be null");
            }
            if (tab.id() == null || tab.id().isBlank()) {
                throw new ValidationException("tab.id cannot be blank");
            }
            if (tab.name() == null || tab.name().isBlank()) {
                throw new ValidationException("tab.name cannot be blank");
            }
            if (tab.icon() == null || tab.icon().isBlank()) {
                throw new ValidationException("tab.icon cannot be blank");
            }
            if (tab.order() == null || tab.order() < 0) {
                throw new ValidationException("tab.order must be >= 0");
            }
            if (tab.enabled() == null) {
                throw new ValidationException("tab.enabled cannot be null");
            }
            if (tab.isSystem() == null) {
                throw new ValidationException("tab.isSystem cannot be null");
            }

            final String id = tab.id().trim();
            if (!ids.add(id)) {
                throw new ValidationException("tab.id must be unique");
            }
            if (SETTINGS_TAB_ID.equals(id)) {
                settingsFound = true;
                if (!tab.enabled()) {
                    throw new ValidationException("settings tab must remain enabled");
                }
                if (!tab.isSystem()) {
                    throw new ValidationException("settings tab must remain system");
                }
            }
            if (INDEX_TAB_ID.equals(id)) {
                indexFound = true;
                if (!tab.enabled()) {
                    throw new ValidationException("index tab must remain enabled");
                }
            }
        }

        if (!settingsFound) {
            throw new ValidationException("settings tab must remain enabled");
        }
        if (!indexFound) {
            throw new ValidationException("index tab must remain enabled");
        }
    }

    private NavigationConfigRepository.NavigationConfigRecord toRecord(
            final long id,
            final NavigationConfigView config,
            final Instant updatedAt
    ) {
        try {
            return new NavigationConfigRepository.NavigationConfigRecord(
                    id,
                    objectMapper.writeValueAsString(config),
                    updatedAt
            );
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid navigation config payload");
        }
    }

    private NavigationConfigView toView(final NavigationConfigRepository.NavigationConfigRecord record) {
        if (record == null || record.configJson() == null || record.configJson().isBlank()) {
            return defaultConfig();
        }
        try {
            return objectMapper.readValue(record.configJson(), NavigationConfigView.class);
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid stored navigation config");
        }
    }
}
