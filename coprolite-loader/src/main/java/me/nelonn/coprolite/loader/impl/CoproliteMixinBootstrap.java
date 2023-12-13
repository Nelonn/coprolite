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

import me.nelonn.coprolite.loader.impl.mixin.CoproliteMixinService;
import me.nelonn.coprolite.loader.impl.mixin.CoproliteMixinServiceBootstrap;
import me.nelonn.coprolite.loader.impl.plugin.PluginContainerImpl;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class CoproliteMixinBootstrap {
    private CoproliteMixinBootstrap() {
        throw new UnsupportedOperationException();
    }

    private static boolean initialized = false;
    private static boolean mixinReady = false;

    public static void init(CoproliteLoaderImpl loader) {
        if (initialized) {
            throw new RuntimeException("CoproliteMixinBootstrap has already been initialized!");
        }

        System.setProperty("mixin.bootstrapService", CoproliteMixinServiceBootstrap.class.getName());
        System.setProperty("mixin.service", CoproliteMixinService.class.getName());

        MixinBootstrap.init();
        //MixinBootstrap.getPlatform().inject();

        /*try (InputStream mappingsInputStream = CoproliteLauncher.getInstance().getResourceAsStream("META-INF/mappings/reobf.tiny")) {
            boolean mojangMapped = false;
            try (InputStream is2 = CoproliteLauncher.getInstance().getResourceAsStream("net/minecraft/server/level/ServerPlayer.class")) {
                mojangMapped = is2 != null;
            }
            if (!mojangMapped) {
                TinyTree mappings;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(mappingsInputStream))) {
                    long time = System.currentTimeMillis();
                    mappings = TinyMappingFactory.loadWithDetection(reader);
                    Log.debug(LogCategory.MAPPINGS, "Loading mappings took %d ms", System.currentTimeMillis() - time);
                }
                System.setProperty("mixin.env.remapRefMap", "true");
                MixinRemapper remapper = new MixinRemapper(mappings, "mojang+yarn", "spigot");
                MixinEnvironment.getDefaultEnvironment().getRemappers().add(remapper);
                Log.info(LogCategory.MIXIN, "Loaded Spigot mappings for mixin remapper!");
            }
        } catch (IOException ignored) {
        }*/

        Map<String, PluginContainerImpl> configToPluginMap = new HashMap<>();

        for (PluginContainerImpl plugin : loader.getPluginsInternal()) {
            for (String config : plugin.getMetadata().getMixinConfigs()) {
                config = plugin.getMetadata().getId() + '|' + config;
                PluginContainerImpl prev = configToPluginMap.putIfAbsent(config, plugin);
                if (prev != null) throw new RuntimeException(String.format("Non-unique Mixin config name %s used by the plugins %s and %s", config, prev.getMetadata().getId(), plugin.getMetadata().getId()));

                try {
                    Mixins.addConfiguration(config);
                } catch (Throwable t) {
                    throw new RuntimeException(String.format("Error creating Mixin config %s for plugin %s", config, plugin.getMetadata().getId()), t);
                }
            }
        }

        initialized = true;
    }

    public static void finishMixinBootstrapping() {
        if (mixinReady) {
            throw new RuntimeException("Must not call CoproliteMixinBootstrap.finishMixinBootstrapping() twice!");
        }

        try {
            Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            m.setAccessible(true);
            m.invoke(null, MixinEnvironment.Phase.INIT);
            m.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mixinReady = true;
    }

    public static boolean isMixinReady() {
        return mixinReady;
    }
}
