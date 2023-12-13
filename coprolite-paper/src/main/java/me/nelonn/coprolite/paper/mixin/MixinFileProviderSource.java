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

package me.nelonn.coprolite.paper.mixin;

import io.papermc.paper.plugin.entrypoint.EntrypointHandler;
import io.papermc.paper.plugin.provider.source.FileProviderSource;
import io.papermc.paper.plugin.provider.type.PluginFileType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.jar.JarFile;

@Mixin(FileProviderSource.class)
public abstract class MixinFileProviderSource {

    @Shadow
    protected abstract Path checkUpdate(Path file) throws Exception;

    @Shadow @Final
    private Function<Path, String> contextChecker;

    /**
     * @author Nelonn
     * @reason The plugin loader throws an exception if the jar file
     * does not contain {@code "plugin.yml"} or {@code "paper-plugin.yml"},
     * but Coprolite uses the {@code "coprolite.plugin.json"} file
     */
    @Overwrite
    public void registerProviders(EntrypointHandler entrypointHandler, Path context) throws Exception {
        String source = this.contextChecker.apply(context);

        if (Files.notExists(context)) {
            throw new IllegalArgumentException(source + " does not exist, cannot load a plugin from it!");
        }

        if (!Files.isRegularFile(context)) {
            throw new IllegalArgumentException(source + " is not a file, cannot load a plugin from it!");
        }

        if (!context.getFileName().toString().endsWith(".jar")) {
            throw new IllegalArgumentException(source + " is not a jar file, cannot load a plugin from it!");
        }

        try {
            context = this.checkUpdate(context);

            JarFile file = new JarFile(context.toFile(), true, JarFile.OPEN_READ, JarFile.runtimeVersion());
            PluginFileType<?, ?> type = PluginFileType.guessType(file);
            if (type == null) {
                return;
                //throw new IllegalArgumentException(source + " does not contain a " + String.join(" or ", PluginFileType.getConfigTypes()) + "! Could not determine plugin type, cannot load a plugin from it!");
            }

            type.register(entrypointHandler, file, context);
        } catch (Exception exception) {
            throw new RuntimeException(source + " failed to load!", exception);
        }
    }
}
