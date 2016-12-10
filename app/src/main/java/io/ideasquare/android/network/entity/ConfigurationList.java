package io.ideasquare.android.network.entity;

import io.ideasquare.android.model.entity.ConfigurationParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class ConfigurationList {

    public List<ConfigurationParam> configuration;

    public ConfigurationList() {
        configuration = new ArrayList<>();
    }

    public static ConfigurationList from(ConfigurationParam... configuration) {
        ConfigurationList configurationList = new ConfigurationList();
        configurationList.configuration = Arrays.asList(configuration);
        return configurationList;
    }

}
