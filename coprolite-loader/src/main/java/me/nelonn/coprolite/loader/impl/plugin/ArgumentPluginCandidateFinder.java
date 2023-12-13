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
import me.nelonn.coprolite.loader.impl.SystemProperties;
import me.nelonn.coprolite.loader.impl.LoaderUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ArgumentPluginCandidateFinder implements PluginCandidateFinder {
    @Override
    public void findPlugins(@NotNull Consumer<Path> out) {
        String list = System.getProperty(SystemProperties.ADD_PLUGINS);
        if (list == null) return;
        addPlugins(list, "system property", out);
    }

    private void addPlugins(@NotNull String list, @NotNull String source, @NotNull Consumer<Path> out) {
        for (String pathStr : list.split(File.pathSeparator)) {
            if (pathStr.isEmpty()) continue;
            addPlugin(pathStr, source, out);
        }
    }

    private void addPlugin(@NotNull String pathStr, @NotNull String source, @NotNull Consumer<Path> out) {
        Path path = LoaderUtil.normalizePath(Paths.get(pathStr));

        if (!Files.exists(path)) { // missing
            Log.warn(LogCategory.DISCOVERY, "Skipping missing %s provided mod path %s", source, path);
        } else if (!DirectoryPluginCandidateFinder.isValidFile(path)) {
            // TODO
        }
        out.accept(path);
    }
}
