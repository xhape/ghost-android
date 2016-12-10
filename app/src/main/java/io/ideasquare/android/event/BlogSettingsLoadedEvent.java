package io.ideasquare.android.event;

import io.ideasquare.android.model.entity.Setting;
import java.util.List;

public class BlogSettingsLoadedEvent {

    public final List<Setting> settings;

    public BlogSettingsLoadedEvent(List<Setting> settings) {
        this.settings = settings;
    }

}
