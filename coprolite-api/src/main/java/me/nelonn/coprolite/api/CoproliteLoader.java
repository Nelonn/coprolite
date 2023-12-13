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

package me.nelonn.coprolite.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public interface CoproliteLoader {

    @NotNull
    static CoproliteLoader getInstance() {
        return Singleton.getInstance();
    }

    /**
     * Get the object share for inter-mod communication.
     *
     * <p>The share allows mods to exchange data without directly referencing each other. This makes simple interaction
     * easier by eliminating any compile- or run-time dependencies if the shared value type is independent of the mod
     * (only Java/game types like collections, primitives, String, Consumer, Function, ...).
     *
     * <p>Active interaction is possible as well since the shared values can be arbitrary Java objects. For example
     * exposing a {@code Runnable} or {@code Function} allows the "API" user to directly invoke some program logic.
     *
     * <p>It is required to prefix the share key with the mod id like {@code mymod:someProperty}. Mods should not
     * modify entries by other mods. The share is thread safe.
     *
     * @return the global object share instance
     */
    @NotNull
    ObjectShare getObjectShare();

    /**
     * Gets the container for a given plugin.
     *
     * @param id the ID of the plugin
     * @return the plugin container, if present
     */
    @NotNull
    Optional<PluginContainer> getPluginContainer(@NotNull String id);

    /**
     * Gets all plugin containers.
     *
     * @return a collection of all loaded plugin containers
     */
    @NotNull
    Collection<PluginContainer> getAllPlugins();

    /**
     * Checks if a plugin with a given ID is loaded.
     *
     * @param id the ID of the plugin, as defined in {@code coprolite.plugin.json}
     * @return whether the plugin is present in this CoproliteLoader instance
     */
    boolean isPluginLoaded(@NotNull String id);

    final class Singleton {
        private static CoproliteLoader instance;

        public static @NotNull CoproliteLoader getInstance() {
            return Objects.requireNonNull(instance);
        }

        public static void setInstance(@NotNull CoproliteLoader instance) {
            if (Singleton.instance != null) {
                throw new UnsupportedOperationException("Cannot redefine singleton CoproliteLoader");
            }
            Singleton.instance = instance;
        }

        private Singleton() {
            throw new UnsupportedOperationException();
        }
    }
}
