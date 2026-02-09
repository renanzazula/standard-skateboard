package com.skateboard.podcast.settings.service.application.adapter.in.rest;

import com.skateboard.podcast.settings.service.application.port.in.SettingsConfigUseCase;
import com.skateboard.podcast.standardbe.api.AdminSettingsConfigApi;
import com.skateboard.podcast.standardbe.api.PublicSettingsConfigApi;
import com.skateboard.podcast.standardbe.api.model.SettingsConfig;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@RestController
public class SettingsConfigController implements PublicSettingsConfigApi, AdminSettingsConfigApi {

    private final SettingsConfigUseCase settingsConfigUseCase;
    private final SettingsConfigApiMapper mapper;

    public SettingsConfigController(
            final SettingsConfigUseCase settingsConfigUseCase,
            final SettingsConfigApiMapper mapper
    ) {
        this.settingsConfigUseCase = settingsConfigUseCase;
        this.mapper = mapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    @GetMapping(
            value = "/public/settings-config",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SettingsConfig> publicSettingsConfigGet() {
        return ResponseEntity.ok(mapper.toApi(settingsConfigUseCase.get()));
    }

    @Override
    @GetMapping(
            value = "/admin/settings-config",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SettingsConfig> adminSettingsConfigGet() {
        return ResponseEntity.ok(mapper.toApi(settingsConfigUseCase.get()));
    }

    @Override
    @PutMapping(
            value = "/admin/settings-config",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SettingsConfig> adminSettingsConfigUpdate(final SettingsConfig settingsConfig) {
        final var updated = settingsConfigUseCase.update(mapper.toView(settingsConfig));
        return ResponseEntity.ok(mapper.toApi(updated));
    }
}
