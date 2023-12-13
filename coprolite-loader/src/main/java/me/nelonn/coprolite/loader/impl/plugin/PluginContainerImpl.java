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

import me.nelonn.coprolite.api.PluginContainer;
import me.nelonn.coprolite.loader.impl.LoaderPluginMetadata;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;

public class PluginContainerImpl implements PluginContainer {
    private final LoaderPluginMetadata metadata;
    private final List<Path> rootPaths;
    private final JarFile jarFile;

    public PluginContainerImpl(@NotNull LoaderPluginMetadata metadata, @NotNull Path path, @NotNull JarFile jarFile) {
        this.metadata = metadata;
        this.rootPaths = List.of(path);
        this.jarFile = jarFile;
    }

    public PluginContainerImpl(@NotNull PluginCandidate candidate) {
        this(candidate.getMetadata(), candidate.getRootPaths().get(0), candidate.getJarFile());
    }

    @Override
    @NotNull
    public LoaderPluginMetadata getMetadata() {
        return metadata;
    }

    @Override
    public @NotNull List<Path> getRootPaths() {
        return rootPaths;
    }

    @NotNull
    public JarFile getJarFile() {
        return jarFile;
    }

    @Override
    public String toString() {
        return String.format("%s %s", metadata.getId(), metadata.getVersion());
    }
}
