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

import me.nelonn.coprolite.loader.impl.log.Log;
import me.nelonn.coprolite.loader.impl.log.LogCategory;
import me.nelonn.coprolite.loader.impl.CoproliteLoaderImpl;
import me.nelonn.coprolite.loader.impl.LoaderPluginMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginDiscoverer {
    private final Path path;

    public PluginDiscoverer(@NotNull Path path) {
        this.path = path;
    }

    public @NotNull List<PluginCandidate> discoverPlugins(@NotNull CoproliteLoaderImpl loader) {
        long startTime = System.nanoTime();

        List<PluginCandidate> candidates = new ArrayList<>();

        Consumer<Path> consumer = filePath -> {
            File file = filePath.toFile();
            try {
                LoaderPluginMetadata metadata = getPluginMetadataJar(filePath);
                if (metadata == null) {
                    return;
                }

                String id = metadata.getId();
                if (id.equalsIgnoreCase("coprolite") ||
                        id.equalsIgnoreCase("paper") ||
                        id.equalsIgnoreCase("spigot") ||
                        id.equalsIgnoreCase("bukkit") ||
                        id.equalsIgnoreCase("minecraft") ||
                        id.equalsIgnoreCase("mojang")) {
                    Log.error(LogCategory.DISCOVERY, "Could not load '" + file.getPath() + "': Restricted Name");
                    return;
                } else if (id.indexOf(' ') != -1) {
                    Log.error(LogCategory.DISCOVERY, "Could not load '" + file.getPath() + "': uses the space-character (0x20) in its name");
                    return;
                }

                JarFile jarFile = new JarFile(file);

                candidates.add(new PluginCandidate(metadata, filePath, jarFile));
            } catch (Exception e) {
                Log.error(LogCategory.DISCOVERY, "Could not load '" + file.getPath() + "'", e);
            }
        };

        new ArgumentPluginCandidateFinder().findPlugins(consumer);
        new DirectoryPluginCandidateFinder(this.path).findPlugins(consumer);

        long endTime = System.nanoTime();

        Log.debug(LogCategory.DISCOVERY, "Plugin discovery time: %.1f ms", (endTime - startTime) * 1e-6);

        return candidates;
    }

    @Nullable
    public LoaderPluginMetadata getPluginMetadataJar(@NotNull Path path) throws IOException {
        try (ZipFile zf = new ZipFile(path.toFile())) {
            ZipEntry entry = zf.getEntry("coprolite.plugin.json");
            if (entry == null) return null;
            try (InputStream is = zf.getInputStream(entry)) {
                return PluginMetadataParser.parseMetadata(is);
            }
        }
    }
}
