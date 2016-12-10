package io.ideasquare.android.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import io.ideasquare.android.model.entity.ConfigurationParam;
import java.lang.reflect.Type;
import java.util.Map;
import io.ideasquare.android.network.entity.ConfigurationList;

/* package */ class ConfigurationListDeserializer implements JsonDeserializer<ConfigurationList> {

    @Override
    public ConfigurationList deserialize(JsonElement element, Type type,
                                         JsonDeserializationContext context)
            throws JsonParseException {
        try {
            JsonArray configJsons = element.getAsJsonObject().getAsJsonArray("configuration");
            if (configJsons.size() > 0 && !configJsons.get(0).getAsJsonObject().has("key")) {
                // new configuration format - dictionary style
                // { configuration: [{ fileStorage: {value: true, type: "bool"}, ... }] }
                return parseDictionaryStyleConfig(configJsons.get(0).getAsJsonObject());
            } else {
                // old configuration format - array of entries
                // { configuration: [ {key: fileStorage, value: true}, ... ] }
                return parseArrayOfEntriesConfig(configJsons);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private ConfigurationList parseDictionaryStyleConfig(JsonObject configJson) {
        ConfigurationList config = new ConfigurationList();
        for (Map.Entry<String, JsonElement> entryJson : configJson.entrySet()) {
            String key = entryJson.getKey();
            JsonPrimitive valueJson = entryJson.getValue().getAsJsonObject()
                    .get("value").getAsJsonPrimitive();
            config.configuration.add(makeConfigParam(key, valueJson));
        }
        return config;
    }

    private ConfigurationList parseArrayOfEntriesConfig(JsonArray configJsons) {
        ConfigurationList config = new ConfigurationList();
        for (JsonElement itemJsonEl : configJsons) {
            JsonObject itemJson = itemJsonEl.getAsJsonObject();
            String key = itemJson.get("key").getAsString();
            JsonPrimitive valueJson = itemJson.getAsJsonPrimitive("value");
            config.configuration.add(makeConfigParam(key, valueJson));
        }
        return config;
    }

    private ConfigurationParam makeConfigParam(String key, JsonPrimitive value)
            throws JsonParseException {
        ConfigurationParam param = new ConfigurationParam();
        String valueStr;
        if (value == null && "mail".equals(key)) {
            valueStr = "";  // Ghost 0.8.0 gives an empty string instead of null, when no mail transport is set
                            // we don't care about the mail transport anyhow
        } else if (value == null) {
            throw new NullPointerException("value for key '" + key + "' is null!");
        } else if (value.isString()) {
            valueStr = value.getAsString();
        } else if (value.isBoolean()) {
            valueStr = String.valueOf(value.getAsBoolean());
        } else if (value.isNumber()) {
            valueStr = String.valueOf(value.getAsDouble());
        } else {
            throw new JsonParseException("unknown value type in Ghost configuration list");
        }
        param.setKey(key);
        param.setValue(valueStr);
        return param;
    }

}
