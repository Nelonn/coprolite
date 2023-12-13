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
import me.nelonn.coprolite.loader.impl.plugin.PluginCandidateFinder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.function.Consumer;

public class DirectoryPluginCandidateFinder implements PluginCandidateFinder {
    private final Path path;

    public DirectoryPluginCandidateFinder(@NotNull Path path) {
        this.path = path;
    }

    @Override
    public void findPlugins(@NotNull Consumer<Path> out) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException("Could not create directory " + path, e);
            }
        }

        if (!Files.isDirectory(path)) {
            throw new RuntimeException(path + " is not a directory!");
        }

        try {
            Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isValidFile(file)) {
                        out.accept(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Exception while searching for plugins in '" + path + "'!", e);
        }
    }

    public static boolean isValidFile(Path path) {
        /*
         * We only propose a file as a possible plugin in the following scenarios:
         * General: Must be a jar file
         *
         * Some OSes Generate metadata so consider the following because of OSes:
         * UNIX: Exclude if file is hidden; this occurs when starting a file name with `.`
         * MacOS: Exclude hidden + startsWith "." since macOS names their metadata files in the form of `.plugin.jar`
         */

        if (!Files.isRegularFile(path)) return false;

        try {
            if (Files.isHidden(path)) return false;
        } catch (IOException e) {
            Log.warn(LogCategory.DISCOVERY, "Error checking if file %s is hidden", path, e);
            return false;
        }

        String fileName = path.getFileName().toString();

        return fileName.endsWith(".jar") && !fileName.startsWith(".");
    }
}
