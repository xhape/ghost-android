package io.ideasquare.android.network.entity;

import io.ideasquare.android.model.entity.Setting;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class SettingsList {

    public List<Setting> settings;

    public static SettingsList from(Setting... settings) {
        SettingsList settingsList = new SettingsList();
        settingsList.settings = Arrays.asList(settings);
        return settingsList;
    }

}
