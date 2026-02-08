package com.skateboard.podcast.appconfig.service.application.adapter.in.rest;

import com.skateboard.podcast.appconfig.service.application.port.in.AppConfigUseCase;
import com.skateboard.podcast.standardbe.api.AdminConfigApi;
import com.skateboard.podcast.standardbe.api.PublicConfigApi;
import com.skateboard.podcast.standardbe.api.model.AppConfig;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@RestController
public class AppConfigController implements PublicConfigApi, AdminConfigApi {

    private final AppConfigUseCase appConfigUseCase;
    private final AppConfigApiMapper mapper;

    public AppConfigController(
            final AppConfigUseCase appConfigUseCase,
            final AppConfigApiMapper mapper
    ) {
        this.appConfigUseCase = appConfigUseCase;
        this.mapper = mapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    @GetMapping(
            value = "/public/app-config",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AppConfig> publicAppConfig() {
        return ResponseEntity.ok(mapper.toApi(appConfigUseCase.get()));
    }

    @Override
    @GetMapping(
            value = "/admin/app-config",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AppConfig> adminAppConfigGet() {
        return ResponseEntity.ok(mapper.toApi(appConfigUseCase.get()));
    }

    @Override
    @PutMapping(
            value = "/admin/app-config",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AppConfig> adminAppConfigUpdate(final AppConfig appConfig) {
        final var updated = appConfigUseCase.update(mapper.toView(appConfig));
        return ResponseEntity.ok(mapper.toApi(updated));
    }
}
