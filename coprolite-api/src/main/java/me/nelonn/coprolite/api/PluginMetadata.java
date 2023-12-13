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

package me.nelonn.coprolite.api;

import me.nelonn.coprolite.api.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface PluginMetadata {

    @NotNull
    String getId();

    Version getVersion();

    String getName();

    Collection<PluginDependency> getDependencies();

    String getDescription();

    Collection<String> getAuthors();

    boolean containsCustomValue(@NotNull String key);

    @Nullable
    CustomValue getCustomValue(@NotNull String key);

    @NotNull
    Map<String, CustomValue> getCustomValues();
}
