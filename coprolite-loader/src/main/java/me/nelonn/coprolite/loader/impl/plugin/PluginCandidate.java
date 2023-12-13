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

import me.nelonn.coprolite.loader.impl.LoaderPluginMetadata;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;

public class PluginCandidate {
    private final LoaderPluginMetadata metadata;
    private final List<Path> rootPaths;
    private final JarFile jarFile;

    public PluginCandidate(@NotNull LoaderPluginMetadata metadata, @NotNull Path path, @NotNull JarFile jarFile) {
        this.metadata = metadata;
        this.rootPaths = List.of(path);
        this.jarFile = jarFile;
    }

    public LoaderPluginMetadata getMetadata() {
        return metadata;
    }

    public List<Path> getRootPaths() {
        return rootPaths;
    }

    public JarFile getJarFile() {
        return jarFile;
    }
}
