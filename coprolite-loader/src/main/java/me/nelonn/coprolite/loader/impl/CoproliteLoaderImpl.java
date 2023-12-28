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

package me.nelonn.coprolite.loader.impl;

import me.nelonn.coprolite.api.ObjectShare;
import me.nelonn.coprolite.api.CoproliteLoader;
import me.nelonn.coprolite.api.PluginContainer;
import me.nelonn.coprolite.api.PluginInitializer;
import me.nelonn.coprolite.loader.api.CoproliteLauncher;
import me.nelonn.coprolite.loader.impl.log.Log;
import me.nelonn.coprolite.loader.impl.log.LogCategory;
import me.nelonn.coprolite.loader.impl.plugin.PluginCandidate;
import me.nelonn.coprolite.loader.impl.plugin.PluginContainerImpl;
import me.nelonn.coprolite.loader.impl.plugin.PluginDiscoverer;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerReader;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CoproliteLoaderImpl implements CoproliteLoader {
    public static CoproliteLoaderImpl INSTANCE = new CoproliteLoaderImpl();

    public static final int ASM_VERSION = Opcodes.ASM9;

    private List<PluginCandidate> pluginCandidates;
    protected final Map<String, PluginContainerImpl> pluginMap = new HashMap<>();
    protected Collection<PluginContainerImpl> plugins = Collections.unmodifiableCollection(pluginMap.values());

    private final ObjectShareImpl objectShare = new ObjectShareImpl();
    private final AccessWidener accessWidener = new AccessWidener();

    private boolean frozen = false;

    private CoproliteLoaderImpl() {
        CoproliteLoader.Singleton.setInstance(this);
    }

    /**
     * Freeze the CoproliteLoader, preventing additional plugins from being loaded.
     */
    public void freeze() {
        if (frozen) {
            throw new IllegalStateException("Already frozen!");
        }

        frozen = true;
        setupPlugins();
    }

    @Override
    public @NotNull ObjectShare getObjectShare() {
        return this.objectShare;
    }

    @Override
    @NotNull
    public Optional<PluginContainer> getPluginContainer(@NotNull String id) {
        return Optional.ofNullable(this.pluginMap.get(id));
    }

    @Override
    @NotNull
    public Collection<PluginContainer> getAllPlugins() {
        return Collections.unmodifiableCollection(this.plugins);
    }

    @Override
    public boolean isPluginLoaded(@NotNull String id) {
        return this.pluginMap.containsKey(id);
    }

    @NotNull
    public Collection<PluginContainerImpl> getPluginsInternal() {
        return this.plugins;
    }

    public AccessWidener getAccessWidener() {
        return this.accessWidener;
    }

    public void load() {
        if (frozen) throw new IllegalStateException("Frozen - cannot load additional plugins!");
        setup();
    }

    private void setup() {
        try {
            Path pluginsPath = Paths.get("./plugins");
            PluginDiscoverer discoverer = new PluginDiscoverer(pluginsPath);
            this.pluginCandidates = discoverer.discoverPlugins(this);

            Log.info(LogCategory.GENERAL, "Loading " + pluginCandidates.size() + " coprolite plugin" + (pluginCandidates.size() > 1 ? "s" : "") + ": " +
                    pluginCandidates.stream().map(it -> it.getMetadata().getId() + ' ' + it.getMetadata().getVersion()).toList());

            for (PluginCandidate mod : this.pluginCandidates) {
                addPlugin(mod);
            }

            this.pluginCandidates = null;
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void addPlugin(@NotNull PluginCandidate candidate) {
        try {
            CoproliteLauncher.getInstance().addToClassPath(candidate.getRootPaths().get(0));
            PluginContainerImpl container = new PluginContainerImpl(candidate);
            pluginMap.put(container.getMetadata().getId(), container);
        } catch (Exception e) {
            Log.error(LogCategory.GENERAL, "Unable to add plugin " + candidate.getMetadata().getId(), e);
        }
    }

    private void setupPlugins() {
        for (PluginContainerImpl plugin : plugins) {
            String entrypoint = plugin.getMetadata().getEntrypoint();
            if (entrypoint == null) continue;
            try {
                Class<?> clazz = Class.forName(entrypoint);

                Class<? extends PluginInitializer> initializerClass;
                try {
                    initializerClass = clazz.asSubclass(PluginInitializer.class);
                } catch (ClassCastException ex) {
                    throw new IllegalStateException("Entrypoint class `" + entrypoint + "' does not extend PluginInitializer", ex);
                }

                PluginInitializer pluginInitializer = initializerClass.getConstructor().newInstance();
                pluginInitializer.onInitialize(plugin);
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Failed to setup plugin %s (%s)", plugin.getMetadata().getName(), plugin.getMetadata().getId()), e);
            }
        }
    }

    public void loadAccessWideners() {
        AccessWidenerReader accessWidenerReader = new AccessWidenerReader(this.accessWidener);

        for (PluginContainer pluginContainer : getAllPlugins()) {
            LoaderPluginMetadata pluginMetadata = (LoaderPluginMetadata) pluginContainer.getMetadata();
            String accessWidener = pluginMetadata.getAccessWidener();
            if (accessWidener == null) continue;

            Path path = pluginContainer.findPath(accessWidener)
                    .orElseThrow(() -> new RuntimeException(String
                            .format("Missing accessWidener file %s from plugin %s",
                                    accessWidener, pluginContainer.getMetadata().getId())));

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                //accessWidenerReader.read(reader, FabricLauncherBase.getLauncher().getTargetNamespace());
                //accessWidenerReader.read(reader, "intermediary");
                accessWidenerReader.read(reader);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read accessWidener file from plugin " + pluginMetadata.getId(), e);
            }
        }
    }
}
