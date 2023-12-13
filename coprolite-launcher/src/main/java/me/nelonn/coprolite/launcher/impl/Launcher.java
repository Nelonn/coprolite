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

import com.google.common.io.ByteStreams;
import me.nelonn.coprolite.launcher.api.IServerProvider;
import me.nelonn.coprolite.launcher.impl.server.PaperclipServerProvider;
import me.nelonn.coprolite.loader.api.CoproliteLauncher;
import me.nelonn.coprolite.loader.impl.CoproliteLoaderImpl;
import me.nelonn.coprolite.loader.impl.CoproliteMixinBootstrap;
import me.nelonn.coprolite.loader.impl.LoaderUtil;
import me.nelonn.coprolite.loader.impl.SystemProperties;
import me.nelonn.coprolite.loader.impl.log.Log;
import me.nelonn.coprolite.loader.impl.log.LogCategory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.util.asm.ASM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Launcher implements CoproliteLauncher {
    public static final String DEFAULT_SERVER = PaperclipServerProvider.NAME;
    private final List<Path> classPath = new ArrayList<>();
    private IServerProvider provider;

    public static void main(String[] args) {
        new Launcher().launch(args);
    }

    public void launch(final @NotNull String[] args) {
        // In FabricMC, each library is stored separately in its jar and, respectively, has its own manifest.
        // Coprolite launcher is delivered as a single file, respectively, libraries do not have their own manifest,
        // the ASM class is completely dependent on the ASM library manifest,
        // so we will determine the ASM version through reflection
        int minorVersion = 6;
        try {
            Field field1 = ASM.class.getDeclaredField("minorVersion");
            Field field2 = ASM.class.getDeclaredField("implMinorVersion");
            field1.setAccessible(true);
            field2.setAccessible(true);
            field1.set(null, minorVersion);
            field2.set(null, minorVersion);
        } catch (Exception e) {
            throw new RuntimeException("Unable to define ASM version through reflection", e);
        }

        CoproliteLauncher.Singleton.setInstance(this);

        classPath.clear();

        List<String> missing = null;
        List<String> unsupported = null;

        for (String cpEntry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (cpEntry.equals("*") || cpEntry.endsWith(File.separator + "*")) {
                if (unsupported == null) {
                    unsupported = new ArrayList<>();
                }
                unsupported.add(cpEntry);
                continue;
            }

            Path path = Paths.get(cpEntry);

            if (!Files.exists(path)) {
                if (missing == null) {
                    missing = new ArrayList<>();
                }
                missing.add(cpEntry);
                continue;
            }

            classPath.add(LoaderUtil.normalizeExistingPath(path));
        }

        // temp
        for (Path path : classPath) {
            addToClassPath(path);
        }

        if (unsupported != null) Log.warn(LogCategory.LAUNCHER, "Knot does not support wildcard class path entries: %s - the game may not load properly!", String.join(", ", unsupported));
        if (missing != null) Log.warn(LogCategory.LAUNCHER, "Class path entries reference missing files: %s - the game may not load properly!", String.join(", ", missing));

        provider = createServerProvider();
        try {
            provider.load(this, args);
        } catch (final Throwable throwable) {
            throw new RuntimeException("Unable to load server provider", throwable);
        }
        Log.finishBuiltinConfig();

        //classLoader = KnotClassLoaderInterface.create(useCompatibility, isDevelopment(), envType, provider);
        //ClassLoader cl = classLoader.getClassLoader();

        CoproliteLoaderImpl loader = CoproliteLoaderImpl.INSTANCE;
        loader.load();
        loader.freeze();

        loader.loadAccessWideners();

        CoproliteMixinBootstrap.init(loader);
        CoproliteMixinBootstrap.finishMixinBootstrapping();

        try {
            provider.execute(args);
        } catch (final Throwable throwable) {
            throw new RuntimeException("Unable to execute server provider", throwable);
        }

    }

    @NotNull
    private IServerProvider createServerProvider() {
        final ServiceLoader<IServerProvider> serviceLoader = ServiceLoader.load(IServerProvider.class,
                Launcher.class.getClassLoader());
        for (Iterator<IServerProvider> iterator = serviceLoader.iterator(); iterator.hasNext(); ) {
            try {
                iterator.next();
            } catch (ServiceConfigurationError e) {
                Log.error(LogCategory.LAUNCHER, "Encountered an exception attempting to load a bootstrap service!", e);
            }
        }
        final Map<String, IServerProvider> providerMap = serviceLoader.stream()
                .collect(Collectors.toMap(provider -> provider.get().name(), ServiceLoader.Provider::get));
        String systemProperty = System.getProperty(SystemProperties.SERVER, DEFAULT_SERVER);
        IServerProvider serverProvider = Objects.requireNonNull(providerMap.get(systemProperty),
                "Server provider '" + systemProperty + "' not found");
        if (!serverProvider.validate()) {
            throw new IllegalStateException("Server provider failed to validate environment!");
        }
        return serverProvider;
    }

    @Override
    public void addToClassPath(Path path, String... allowedPrefixes) {
        Log.debug(LogCategory.LAUNCHER, "Adding " + path + " to classpath.");

        try {
            CoproliteAgent.addJar(path);
        } catch (Exception e) {
            Log.error(LogCategory.LAUNCHER, "Unable to add " + path + " to claspath", e);
        }
        //classLoader.setAllowedPrefixes(path, allowedPrefixes);
        //classLoader.addCodeSource(path);
    }

    @Override
    public boolean isClassLoaded(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    @Override
    public ClassLoader getTargetClassLoader() {
        return Launcher.class.getClassLoader();
    }

    public byte[] getRawClassBytes(String name) throws IOException {
        try (InputStream is = getResourceAsStream(LoaderUtil.getClassFileName(name))) {
            if (is == null) throw new FileNotFoundException("Class not found: " + name);
            return ByteStreams.toByteArray(is);
        }
    }

    public IServerProvider getProvider() {
        return provider;
    }

    @Override
    public byte[] getClassByteArray(String name, boolean runTransformers) throws IOException {
        if (runTransformers) {
            name = name.replace('/', '.');
            byte[] input = null;
            if (provider.getTransformer() != null) {
                input = provider.getTransformer().transform(name);
                if (input != null) {
                    System.out.println("GET: " + name);
                }
            }
            if (input == null) {
                try {
                    input = getRawClassBytes(name);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load class file for '" + name + "'!", e);
                }
            }
            return input;
        } else {
            return getRawClassBytes(name);
        }
    }
}
