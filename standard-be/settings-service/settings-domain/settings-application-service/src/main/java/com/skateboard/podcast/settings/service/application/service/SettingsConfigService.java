package com.skateboard.podcast.settings.service.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.domain.exception.ValidationException;
import com.skateboard.podcast.settings.service.application.dto.LanguageConfigView;
import com.skateboard.podcast.settings.service.application.dto.LanguageView;
import com.skateboard.podcast.settings.service.application.dto.ProfileConfigView;
import com.skateboard.podcast.settings.service.application.dto.SessionConfigView;
import com.skateboard.podcast.settings.service.application.dto.SettingsAuthMethodsView;
import com.skateboard.podcast.settings.service.application.dto.SettingsConfigView;
import com.skateboard.podcast.settings.service.application.dto.SettingsServiceModesView;
import com.skateboard.podcast.settings.service.application.port.in.SettingsConfigUseCase;
import com.skateboard.podcast.settings.service.application.port.out.SettingsConfigRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SettingsConfigService implements SettingsConfigUseCase {

    private static final long CONFIG_ID = 1L;
    private static final int MIN_SESSION_MS = 300000;
    private static final int MAX_SESSION_MS = 86400000;

    private final SettingsConfigRepository repository;
    private final ObjectMapper objectMapper;

    public SettingsConfigService(
            final SettingsConfigRepository repository,
            final ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public SettingsConfigView get() {
        return toView(loadOrCreate());
    }

    @Override
    public SettingsConfigView update(final SettingsConfigView updated) {
        validate(updated);
        final SettingsConfigRepository.SettingsConfigRecord existing = loadOrCreate();
        final SettingsConfigRepository.SettingsConfigRecord saved = repository.save(
                toRecord(existing.id(), updated, existing.updatedAt())
        );
        return toView(saved);
    }

    private SettingsConfigRepository.SettingsConfigRecord loadOrCreate() {
        return repository.findById(CONFIG_ID)
                .orElseGet(this::createDefault);
    }

    private SettingsConfigRepository.SettingsConfigRecord createDefault() {
        final SettingsConfigView view = defaultConfig();
        final SettingsConfigRepository.SettingsConfigRecord record = toRecord(CONFIG_ID, view, null);
        return repository.save(record);
    }

    private SettingsConfigView defaultConfig() {
        return new SettingsConfigView(
                new SettingsAuthMethodsView(false, false, true),
                new SettingsServiceModesView("mock", "mock", "mock"),
                new SessionConfigView(1800000, 900000, true),
                new LanguageConfigView(
                        List.of(
                                new LanguageView("en", "English", "English", "EN"),
                                new LanguageView("es", "Spanish", "Espanol", "ES")
                        ),
                        new LanguageView("en", "English", "English", "EN")
                ),
                new ProfileConfigView(
                        3,
                        30,
                        5,
                        List.of("image/png", "image/jpeg", "image/jpg", "image/svg+xml")
                )
        );
    }

    private void validate(final SettingsConfigView config) {
        if (config == null) {
            throw new ValidationException("settings config cannot be null");
        }

        final SettingsAuthMethodsView authMethods = config.enabledAuthMethods();
        if (authMethods == null) {
            throw new ValidationException("enabledAuthMethods cannot be null");
        }
        if (authMethods.google() == null || authMethods.apple() == null || authMethods.manual() == null) {
            throw new ValidationException("enabledAuthMethods cannot contain nulls");
        }
        if (!authMethods.google() && !authMethods.apple() && !authMethods.manual()) {
            throw new ValidationException("at least one auth method must be enabled");
        }

        final SettingsServiceModesView serviceModes = config.serviceModes();
        if (serviceModes == null) {
            throw new ValidationException("serviceModes cannot be null");
        }
        validateMode(serviceModes.google(), "serviceModes.google");
        validateMode(serviceModes.apple(), "serviceModes.apple");
        validateMode(serviceModes.manual(), "serviceModes.manual");

        final SessionConfigView sessionConfig = config.sessionConfig();
        if (sessionConfig == null) {
            throw new ValidationException("sessionConfig cannot be null");
        }
        if (sessionConfig.maxTime() == null || sessionConfig.idleTime() == null) {
            throw new ValidationException("sessionConfig times cannot be null");
        }
        if (sessionConfig.autoRefresh() == null) {
            throw new ValidationException("sessionConfig.autoRefresh cannot be null");
        }
        if (sessionConfig.maxTime() < MIN_SESSION_MS || sessionConfig.maxTime() > MAX_SESSION_MS) {
            throw new ValidationException("sessionConfig.maxTime out of range");
        }
        if (sessionConfig.idleTime() < MIN_SESSION_MS || sessionConfig.idleTime() > MAX_SESSION_MS) {
            throw new ValidationException("sessionConfig.idleTime out of range");
        }

        final LanguageConfigView languageConfig = config.languageConfig();
        if (languageConfig == null) {
            throw new ValidationException("languageConfig cannot be null");
        }
        if (languageConfig.availableLanguages() == null || languageConfig.availableLanguages().isEmpty()) {
            throw new ValidationException("languageConfig.availableLanguages cannot be empty");
        }
        if (languageConfig.defaultLanguage() == null) {
            throw new ValidationException("languageConfig.defaultLanguage cannot be null");
        }
        final Set<String> codes = new HashSet<>();
        for (final LanguageView language : languageConfig.availableLanguages()) {
            if (language == null) {
                throw new ValidationException("language cannot be null");
            }
            final String code = normalizeRequired(language.code(), "language.code");
            normalizeRequired(language.name(), "language.name");
            normalizeRequired(language.nativeName(), "language.nativeName");
            normalizeRequired(language.flag(), "language.flag");
            if (!codes.add(code.toLowerCase(Locale.ROOT))) {
                throw new ValidationException("language.code must be unique");
            }
        }
        final String defaultCode = normalizeRequired(languageConfig.defaultLanguage().code(), "defaultLanguage.code");
        if (!codes.contains(defaultCode.toLowerCase(Locale.ROOT))) {
            throw new ValidationException("defaultLanguage must be in availableLanguages");
        }

        final ProfileConfigView profileConfig = config.profileConfig();
        if (profileConfig == null) {
            throw new ValidationException("profileConfig cannot be null");
        }
        if (profileConfig.usernameMinLength() == null || profileConfig.usernameMaxLength() == null) {
            throw new ValidationException("profileConfig username limits cannot be null");
        }
        if (profileConfig.usernameMinLength() < 1) {
            throw new ValidationException("profileConfig.usernameMinLength out of range");
        }
        if (profileConfig.usernameMaxLength() <= profileConfig.usernameMinLength()) {
            throw new ValidationException("profileConfig.usernameMaxLength must be greater than min");
        }
        if (profileConfig.avatarMaxSizeMB() == null || profileConfig.avatarMaxSizeMB() < 1) {
            throw new ValidationException("profileConfig.avatarMaxSizeMB out of range");
        }
        if (profileConfig.allowedAvatarFormats() == null || profileConfig.allowedAvatarFormats().isEmpty()) {
            throw new ValidationException("profileConfig.allowedAvatarFormats cannot be empty");
        }
        for (final String format : profileConfig.allowedAvatarFormats()) {
            normalizeRequired(format, "profileConfig.allowedAvatarFormats");
        }
    }

    private static void validateMode(final String mode, final String fieldName) {
        final String normalized = normalizeRequired(mode, fieldName).toLowerCase(Locale.ROOT);
        if (!"mock".equals(normalized) && !"real".equals(normalized)) {
            throw new ValidationException(fieldName + " must be mock or real");
        }
    }

    private static String normalizeRequired(final String value, final String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    private SettingsConfigRepository.SettingsConfigRecord toRecord(
            final long id,
            final SettingsConfigView config,
            final Instant updatedAt
    ) {
        try {
            return new SettingsConfigRepository.SettingsConfigRecord(
                    id,
                    objectMapper.writeValueAsString(config),
                    updatedAt
            );
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid settings config payload");
        }
    }

    private SettingsConfigView toView(final SettingsConfigRepository.SettingsConfigRecord record) {
        if (record == null || record.configJson() == null || record.configJson().isBlank()) {
            return defaultConfig();
        }
        try {
            return objectMapper.readValue(record.configJson(), SettingsConfigView.class);
        } catch (final JsonProcessingException e) {
            throw new ValidationException("invalid stored settings config");
        }
    }
}
