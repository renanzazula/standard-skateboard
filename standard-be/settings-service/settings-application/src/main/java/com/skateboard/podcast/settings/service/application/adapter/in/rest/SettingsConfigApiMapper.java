package com.skateboard.podcast.settings.service.application.adapter.in.rest;

import com.skateboard.podcast.settings.service.application.dto.LanguageConfigView;
import com.skateboard.podcast.settings.service.application.dto.LanguageView;
import com.skateboard.podcast.settings.service.application.dto.ProfileConfigView;
import com.skateboard.podcast.settings.service.application.dto.SessionConfigView;
import com.skateboard.podcast.settings.service.application.dto.SettingsAuthMethodsView;
import com.skateboard.podcast.settings.service.application.dto.SettingsConfigView;
import com.skateboard.podcast.settings.service.application.dto.SettingsServiceModesView;
import com.skateboard.podcast.standardbe.api.model.Language;
import com.skateboard.podcast.standardbe.api.model.LanguageConfig;
import com.skateboard.podcast.standardbe.api.model.ProfileConfig;
import com.skateboard.podcast.standardbe.api.model.SessionConfig;
import com.skateboard.podcast.standardbe.api.model.ServiceMode;
import com.skateboard.podcast.standardbe.api.model.SettingsAuthMethods;
import com.skateboard.podcast.standardbe.api.model.SettingsConfig;
import com.skateboard.podcast.standardbe.api.model.SettingsServiceModes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettingsConfigApiMapper {

    public SettingsConfig toApi(final SettingsConfigView view) {
        if (view == null) {
            return null;
        }
        return new SettingsConfig()
                .enabledAuthMethods(toApi(view.enabledAuthMethods()))
                .serviceModes(toApi(view.serviceModes()))
                .sessionConfig(toApi(view.sessionConfig()))
                .languageConfig(toApi(view.languageConfig()))
                .profileConfig(toApi(view.profileConfig()));
    }

    public AdminSettingsConfig toAdminApi(final SettingsConfigView view) {
        if (view == null) {
            return null;
        }
        return new AdminSettingsConfig()
                .authMethods(toApi(view.enabledAuthMethods()))
                .authServiceModes(toApi(view.serviceModes()))
                .sessionConfig(toApi(view.sessionConfig()))
                .languageConfig(toApi(view.languageConfig()))
                .profileConfig(toApi(view.profileConfig()))
                .feedRealtimeEnabled(view.feedRealtimeEnabled());
    }

    public SettingsConfigView toView(final SettingsConfig api) {
        if (api == null) {
            return null;
        }
        return new SettingsConfigView(
                toView(api.getEnabledAuthMethods()),
                toView(api.getServiceModes()),
                toView(api.getSessionConfig()),
                toView(api.getLanguageConfig()),
                toView(api.getProfileConfig()),
                null
        );
    }

    public SettingsConfigView toView(final AdminSettingsConfig api) {
        if (api == null) {
            return null;
        }
        return new SettingsConfigView(
                toView(api.getAuthMethods()),
                toView(api.getAuthServiceModes()),
                toView(api.getSessionConfig()),
                toView(api.getLanguageConfig()),
                toView(api.getProfileConfig()),
                api.getFeedRealtimeEnabled()
        );
    }

    private SettingsAuthMethods toApi(final SettingsAuthMethodsView view) {
        if (view == null) {
            return null;
        }
        return new SettingsAuthMethods()
                .google(view.google())
                .apple(view.apple())
                .manual(view.manual());
    }

    private SettingsAuthMethodsView toView(final SettingsAuthMethods api) {
        if (api == null) {
            return null;
        }
        return new SettingsAuthMethodsView(
                api.getGoogle(),
                api.getApple(),
                api.getManual()
        );
    }

    private SettingsServiceModes toApi(final SettingsServiceModesView view) {
        if (view == null) {
            return null;
        }
        return new SettingsServiceModes()
                .google(toServiceMode(view.google()))
                .apple(toServiceMode(view.apple()))
                .manual(toServiceMode(view.manual()));
    }

    private SettingsServiceModesView toView(final SettingsServiceModes api) {
        if (api == null) {
            return null;
        }
        return new SettingsServiceModesView(
                toServiceModeValue(api.getGoogle()),
                toServiceModeValue(api.getApple()),
                toServiceModeValue(api.getManual())
        );
    }

    private SessionConfig toApi(final SessionConfigView view) {
        if (view == null) {
            return null;
        }
        return new SessionConfig()
                .maxTime(view.maxTime())
                .idleTime(view.idleTime())
                .autoRefresh(view.autoRefresh());
    }

    private SessionConfigView toView(final SessionConfig api) {
        if (api == null) {
            return null;
        }
        return new SessionConfigView(
                api.getMaxTime(),
                api.getIdleTime(),
                api.getAutoRefresh()
        );
    }

    private LanguageConfig toApi(final LanguageConfigView view) {
        if (view == null) {
            return null;
        }
        final List<Language> languages = view.availableLanguages() == null
                ? List.of()
                : view.availableLanguages().stream().map(this::toApi).toList();
        return new LanguageConfig()
                .availableLanguages(languages)
                .defaultLanguage(toApi(view.defaultLanguage()));
    }

    private LanguageConfigView toView(final LanguageConfig api) {
        if (api == null) {
            return null;
        }
        final List<LanguageView> languages = api.getAvailableLanguages() == null
                ? List.of()
                : api.getAvailableLanguages().stream().map(this::toView).toList();
        return new LanguageConfigView(
                languages,
                toView(api.getDefaultLanguage())
        );
    }

    private Language toApi(final LanguageView view) {
        if (view == null) {
            return null;
        }
        return new Language()
                .code(view.code())
                .name(view.name())
                .nativeName(view.nativeName())
                .flag(view.flag());
    }

    private LanguageView toView(final Language api) {
        if (api == null) {
            return null;
        }
        return new LanguageView(
                api.getCode(),
                api.getName(),
                api.getNativeName(),
                api.getFlag()
        );
    }

    private ProfileConfig toApi(final ProfileConfigView view) {
        if (view == null) {
            return null;
        }
        return new ProfileConfig()
                .usernameMinLength(view.usernameMinLength())
                .usernameMaxLength(view.usernameMaxLength())
                .avatarMaxSizeMB(view.avatarMaxSizeMB())
                .allowedAvatarFormats(view.allowedAvatarFormats());
    }

    private ProfileConfigView toView(final ProfileConfig api) {
        if (api == null) {
            return null;
        }
        return new ProfileConfigView(
                api.getUsernameMinLength(),
                api.getUsernameMaxLength(),
                api.getAvatarMaxSizeMB(),
                api.getAllowedAvatarFormats()
        );
    }

    private static ServiceMode toServiceMode(final String mode) {
        if (mode == null) {
            return null;
        }
        return ServiceMode.fromValue(mode);
    }

    private static String toServiceModeValue(final ServiceMode mode) {
        return mode == null ? null : mode.getValue();
    }
}
