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

package me.nelonn.coprolite.launcher.impl.server;

import me.nelonn.coprolite.launcher.api.IServerProvider;
import me.nelonn.coprolite.launcher.impl.GameTransformer;
import me.nelonn.coprolite.loader.api.CoproliteLauncher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PaperclipServerProvider implements IServerProvider {
    public static final String NAME = "paperclip";
    public static final String JAR_PROPERTY = "coprolite.jar";
    public static final String DEFAULT_JAR = "./server.jar";
    public static final String PAPERCLIP_MAIN = System.getProperty("coprolite.paperclip.main", "io.papermc.paperclip.Paperclip");
    private Class<?> paperclipClass;
    //private final GameTransformer transformer = new GameTransformer(new PaperPluginPatch());
    private final GameTransformer transformer = null;

    @Override
    public @NotNull String name() {
        return NAME;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public @Nullable GameTransformer getTransformer() {
        return transformer;
    }

    @Override
    public void load(CoproliteLauncher launcher, String[] args) throws Throwable {
        String jar = System.getProperty(JAR_PROPERTY);
        Path jarPath = null;
        if (jar != null) {
            jarPath = Paths.get(jar);
        } else {
            if (args.length > 0) {
                String firstArg = args[0];
                if (!firstArg.equalsIgnoreCase("nogui")) {
                    File file = new File(args[0]);
                    if (file.exists()) {
                        jarPath = file.toPath();
                    }
                }
            }
            if (jarPath == null) {
                jarPath = Paths.get(DEFAULT_JAR);
            }
        }

        launcher.addToClassPath(jarPath);

        paperclipClass = CoproliteLauncher.getInstance().getTargetClassLoader().loadClass(PAPERCLIP_MAIN);
        Method setupClasspath = paperclipClass.getDeclaredMethod("setupClasspath");
        setupClasspath.setAccessible(true);
        List<Path> classPath = Arrays.stream((URL[]) setupClasspath.invoke(null)).map(url -> {
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        if (transformer != null) {
            transformer.locateEntrypoints(launcher, classPath);
        }
        for (Path path : classPath) {
            launcher.addToClassPath(path);
        }
        /*if (transformer != null) {
            transformer.locateEntrypoints(launcher, classPath);
        }*/
    }

    @Override
    public void execute(String[] args) {
        final String mainClassName;
        try {
            Method findMainClass = paperclipClass.getDeclaredMethod("findMainClass");
            findMainClass.trySetAccessible();
            mainClassName = (String) findMainClass.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Starting " + mainClassName);

        try {
            Class.forName("org.bukkit.plugin.java.JavaPlugin");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread runThread = new Thread(() -> {
            try {
                //final Class<?> mainClass = Class.forName(mainClassName, true, classLoader);
                final Class<?> mainClass = Class.forName(mainClassName);
                final MethodHandle mainHandle = MethodHandles.lookup()
                        .findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class))
                        .asFixedArity();
                mainHandle.invoke((Object) args);
            } catch (final Throwable t) {
                throw new RuntimeException(t);
            }
        }, "ServerMain");
        runThread.setContextClassLoader(CoproliteLauncher.getInstance().getTargetClassLoader());
        runThread.start();
    }
}
