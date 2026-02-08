package com.skateboard.podcast.appconfig.service.application.adapter.in.rest;

import com.skateboard.podcast.appconfig.service.application.dto.NavigationConfigView;
import com.skateboard.podcast.appconfig.service.application.dto.NavigationTabView;
import com.skateboard.podcast.standardbe.api.model.NavigationConfig;
import com.skateboard.podcast.standardbe.api.model.NavigationTab;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NavigationConfigApiMapper {

    public NavigationConfig toApi(final NavigationConfigView view) {
        if (view == null) {
            return null;
        }
        final List<NavigationTab> tabs = view.tabs() == null
                ? List.of()
                : view.tabs().stream().map(this::toApi).toList();
        return new NavigationConfig().tabs(tabs);
    }

    public NavigationConfigView toView(final NavigationConfig api) {
        if (api == null) {
            return null;
        }
        final List<NavigationTabView> tabs = api.getTabs() == null
                ? List.of()
                : api.getTabs().stream().map(this::toView).toList();
        return new NavigationConfigView(tabs);
    }

    private NavigationTab toApi(final NavigationTabView view) {
        if (view == null) {
            return null;
        }
        return new NavigationTab()
                .id(view.id())
                .name(view.name())
                .icon(view.icon())
                .order(view.order())
                .enabled(view.enabled())
                .isSystem(view.isSystem());
    }

    private NavigationTabView toView(final NavigationTab api) {
        if (api == null) {
            return null;
        }
        return new NavigationTabView(
                api.getId(),
                api.getName(),
                api.getIcon(),
                api.getOrder(),
                api.getEnabled(),
                api.getIsSystem()
        );
    }
}
