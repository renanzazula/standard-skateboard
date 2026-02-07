package com.skateboard.podcast.standard.service.container.appconfig;

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

    private final AppConfigStore store;

    public AppConfigController(final AppConfigStore store) {
        this.store = store;
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
        return ResponseEntity.ok(store.get());
    }

    @Override
    @GetMapping(
            value = "/admin/app-config",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AppConfig> adminAppConfigGet() {
        return ResponseEntity.ok(store.get());
    }

    @Override
    @PutMapping(
            value = "/admin/app-config",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AppConfig> adminAppConfigUpdate(final AppConfig appConfig) {
        return ResponseEntity.ok(store.update(appConfig));
    }
}
