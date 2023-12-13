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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.nelonn.coprolite.api.CustomValue;
import me.nelonn.coprolite.api.PluginDependency;
import me.nelonn.coprolite.api.version.Version;
import me.nelonn.coprolite.api.version.VersionParsingException;
import me.nelonn.coprolite.loader.impl.VersionImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class V0PluginMetadataParser {
    public V0PluginMetadata deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        String id = json.get("id").getAsString();
        Version version = json.has("version") ? new VersionImpl(json.get("version").getAsString()) : null;
        String name = json.has("name") ? json.get("name").getAsString() : null;
        List<PluginDependency> dependencies = new ArrayList<>();
        if (json.has("depends")) {
            for (Map.Entry<String, JsonElement> depend : json.getAsJsonObject("depends").entrySet()) {
                try {
                    dependencies.add(new PluginDependencyImpl(depend.getKey(), depend.getValue().getAsString()));
                } catch (VersionParsingException e) {
                    throw new JsonParseException(e);
                }
            }
        }
        String description = json.has("description") ? json.get("description").getAsString() : null;
        Collection<String> authors = json.has("authors") ? StreamSupport
                .stream(json.getAsJsonArray("authors").spliterator(), false)
                .map(JsonElement::getAsString).collect(Collectors.toSet()) : Collections.emptyList();
        Map<String, CustomValue> customValues = new LinkedHashMap<>();
        if (json.has("custom")) {
            JsonObject jsonObject = json.getAsJsonObject("custom");
            for (Map.Entry<String, JsonElement> it : jsonObject.entrySet()) {
                customValues.put(it.getKey(), CustomValueImpl.read(it.getValue()));
            }
        }
        String entrypoint = json.has("entrypoint") ? json.get("entrypoint").getAsString() : null;
        String accessWidener = json.has("accessWidener") ? json.get("accessWidener").getAsString() : null;
        Collection<V0PluginMetadata.MixinEntry> mixins = json.has("mixins") ? StreamSupport
                .stream(json.getAsJsonArray("mixins").spliterator(), false)
                .map(it -> new V0PluginMetadata.MixinEntry(it.getAsString())).collect(Collectors.toSet()) :
                Collections.emptyList();
        return new V0PluginMetadata(id, version, name, dependencies, description, authors, customValues, entrypoint, accessWidener, mixins);
    }
}
