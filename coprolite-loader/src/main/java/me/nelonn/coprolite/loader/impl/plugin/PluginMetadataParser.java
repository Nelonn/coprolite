/*
 * Copyright 2023 Michael Neonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.nelonn.coprolite.loader.impl.plugin;

import com.google.gson.*;
import me.nelonn.coprolite.loader.impl.LoaderPluginMetadata;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class PluginMetadataParser implements JsonDeserializer<LoaderPluginMetadata> {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(LoaderPluginMetadata.class, new PluginMetadataParser()).create();

    public static LoaderPluginMetadata parseMetadata(@NotNull InputStream is) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return parseMetadata(isr);
        }
    }

    public static LoaderPluginMetadata parseMetadata(@NotNull Reader reader) {
        return GSON.fromJson(reader, LoaderPluginMetadata.class);
    }

    private final V0PluginMetadataParser v0PluginMetadataParser = new V0PluginMetadataParser();

    @Override
    public LoaderPluginMetadata deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        int schemaVersion = jsonObject.get("schemaVersion").getAsInt();
        if (schemaVersion == 0) {
            return v0PluginMetadataParser.deserialize(jsonObject, context);
        } else {
            throw new JsonParseException("Unknown schema version '" + schemaVersion + "'");
        }
    }
}
