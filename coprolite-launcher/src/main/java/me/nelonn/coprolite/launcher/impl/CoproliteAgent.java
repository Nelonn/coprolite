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

package me.nelonn.coprolite.launcher.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.tools.agent.MixinAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarFile;

public final class CoproliteAgent {
    /**
     * The agents launch instrumentation.
     */
    private static Instrumentation INSTRUMENTATION = null;

    public static void addTransformer(final @NotNull ClassFileTransformer transformer) {
        if (INSTRUMENTATION != null) {
            INSTRUMENTATION.addTransformer(transformer, true);
        }
    }

    /**
     * Adds the specified JAR file to the system class loader.
     *
     * @param path The path to the JAR file
     * @throws IOException If the target cannot be added
     */
    public static void addJar(final @NotNull Path path) throws IOException {
        final File file = path.toFile();
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
        if (file.isDirectory() || !file.getName().endsWith(".jar")) throw new IOException("Provided path is not a jar file: " + path);
        addJar(new JarFile(file));
    }

    /**
     * Adds the specified JAR file to the system class loader.
     *
     * @param file The JAR file
     * @throws IOException If the target cannot be added
     */
    public static void addJar(final @NotNull JarFile file) throws IOException {
        if (INSTRUMENTATION != null) {
            INSTRUMENTATION.appendToSystemClassLoaderSearch(file);
            return;
        }
        throw new IOException("Unable to addJar for '" + file.getName() + "'.");
    }

    public static void init(Instrumentation instrumentation) {
        if (INSTRUMENTATION == null) {
            INSTRUMENTATION = Objects.requireNonNull(instrumentation, "instrumentation");
        }
        addTransformer(new Transformer());
    }

    /**
     * The agent premain is called by the JRE.
     *
     * @param agentArgs The agent arguments
     * @param instrumentation The instrumentation
     */
    public static void premain(final @NotNull String agentArgs, final @Nullable Instrumentation instrumentation) {
        System.setProperty("mixin.hotSwap", "true");
        init(instrumentation);
        MixinAgent.premain(agentArgs, instrumentation);
    }

    /**
     * The agent main is called by the JRE.
     *
     * <p>You should launch the agent in premain!</p>
     *
     * @param agentArgs The agent arguments
     * @param instrumentation The instrumentation
     */
    public static void agentmain(final @NotNull String agentArgs, final @Nullable Instrumentation instrumentation) {
        init(instrumentation);
        MixinAgent.agentmain(agentArgs, instrumentation);
    }

    private CoproliteAgent() {
    }
}
