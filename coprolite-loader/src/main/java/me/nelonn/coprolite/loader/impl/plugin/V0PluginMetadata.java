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

import me.nelonn.coprolite.api.CustomValue;
import me.nelonn.coprolite.api.PluginDependency;
import me.nelonn.coprolite.api.version.Version;
import me.nelonn.coprolite.loader.impl.LoaderPluginMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class V0PluginMetadata implements LoaderPluginMetadata {
    private final String id;
    private final Version version;
    private final String name;
    private final Collection<PluginDependency> dependencies;
    private final String description;
    private final Collection<String> authors;

    private final Map<String, CustomValue> customValues;

    private final String entrypoint;
    private final String accessWidener;
    private final Collection<MixinEntry> mixins;

    public V0PluginMetadata(@NotNull String id,
                            Version version,
                            String name,
                            Collection<PluginDependency> dependencies,
                            String description,
                            Collection<String> authors,
                            Map<String, CustomValue> customValues,
                            @Nullable String entrypoint,
                            @Nullable String accessWidener,
                            Collection<MixinEntry> mixins) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.dependencies = Collections.unmodifiableCollection(dependencies);
        this.description = description;
        this.authors = Collections.unmodifiableCollection(authors);
        this.customValues = Collections.unmodifiableMap(customValues);
        this.entrypoint = entrypoint;
        this.accessWidener = accessWidener;
        this.mixins = Collections.unmodifiableCollection(mixins);
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public Version getVersion() {
        return this.version;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<PluginDependency> getDependencies() {
        return this.dependencies;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Collection<String> getAuthors() {
        return this.authors;
    }

    @Override
    public boolean containsCustomValue(@NotNull String key) {
        return getCustomValues().containsKey(key);
    }

    @Override
    public @Nullable CustomValue getCustomValue(@NotNull String key) {
        return getCustomValues().get(key);
    }

    @Override
    public @NotNull Map<String, CustomValue> getCustomValues() {
        return this.customValues;
    }

    @Override
    public @Nullable String getEntrypoint() {
        return this.entrypoint;
    }

    public @Nullable String getAccessWidener() {
        return this.accessWidener;
    }

    @NotNull
    public Collection<String> getMixinConfigs() {
        final List<String> mixinConfigs = new ArrayList<>();

        // This is only ever called once, so no need to store the result of this.
        for (MixinEntry mixin : this.mixins) {
            mixinConfigs.add(mixin.config);
        }

        return mixinConfigs;
    }

    public static final class MixinEntry {
        private final String config;

        public MixinEntry(String config) {
            this.config = config;
        }

        public String getConfig() {
            return config;
        }
    }
}
