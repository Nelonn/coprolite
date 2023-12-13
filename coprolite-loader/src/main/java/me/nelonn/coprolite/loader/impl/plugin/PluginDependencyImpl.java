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

import me.nelonn.coprolite.api.PluginDependency;
import me.nelonn.coprolite.api.version.Version;
import me.nelonn.coprolite.api.version.VersionInterval;
import me.nelonn.coprolite.api.version.VersionParsingException;
import me.nelonn.coprolite.api.version.VersionPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PluginDependencyImpl implements PluginDependency {
    private final String pluginId;
    private final String matcherString;
    private final Collection<VersionPredicate> ranges;

    public PluginDependencyImpl(@NotNull String pluginId, @NotNull String matcherString) throws VersionParsingException {
        this.pluginId = pluginId;
        this.matcherString = matcherString;
        this.ranges = List.of(VersionPredicate.parse(this.matcherString));
    }

    @Override
    public @NotNull String getPluginId() {
        return this.pluginId;
    }

    @Override
    public boolean matches(@NotNull Version version) {
        for (VersionPredicate predicate : ranges) {
            if (predicate.test(version)) return true;
        }

        return false;
    }

    @Override
    public Collection<VersionPredicate> getVersionRequirements() {
        return ranges;
    }

    @Override
    public List<VersionInterval> getVersionIntervals() {
        List<VersionInterval> ret = Collections.emptyList();

        for (VersionPredicate predicate : ranges) {
            ret = VersionInterval.or(ret, predicate.getInterval());
        }

        return ret;
    }

    @Override
    public String toString() {
        return this.pluginId + " @ " + this.matcherString;
    }
}
