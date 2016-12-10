package io.ideasquare.android.event;

import java.util.List;

import io.ideasquare.android.model.entity.ConfigurationParam;

public class ConfigurationLoadedEvent {

    public final List<ConfigurationParam> params;

    public ConfigurationLoadedEvent(List<ConfigurationParam> params) {
        this.params = params;
    }

}
