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
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Object share for inter-plugin communication, obtainable through {@link CoproliteLoader#getObjectShare()}.
 *
 * <p>The share allows plugins to exchange data without directly referencing each other. This makes simple interaction
 * easier by eliminating any compile- or run-time dependencies if the shared value type is independent of the plugin
 * (only Java/game types like collections, primitives, String, Consumer, Function, ...).
 *
 * <p>Active interaction is possible as well since the shared values can be arbitrary Java objects. For example
 * exposing a {@code Runnable} or {@code Function} allows the "API" user to directly invoke some program logic.
 *
 * <p>It is required to prefix the share key with the plugin id like {@code myplugin:some_property}. Plugins should not
 * modify entries by other plugins. The share is thread safe.
 */
public interface ObjectShare {
    /**
     * Get the value for a specific key.
     *
     * <p>Java 16 introduced a convenient syntax for type safe queries that combines null check, type check and cast:
     * <pre>
     * if (CoproliteLoader.getInstance().getObjectShare().get("some_plugin:some_value") instanceof String value) {
     *   // use value here
     * }
     * </pre>
     *
     * <p>A generic type still needs a second unchecked cast due to erasure:
     * <pre>
     * if (CoproliteLoader.getInstance().getObjectShare().get("myplugin:fuel") instanceof Consumer{@code<?>} c) {
     *   ((Consumer{@code<ItemStack>}) c).accept(someStack);
     * }
     * </pre>
     *
     * <p>Consider using {@link #whenAvailable} instead if the value may not be available yet. The plugin load order is
     * undefined, so entries that are added during the same load phase should be queried in a later phase or be handled
     * through {@link #whenAvailable(String, BiConsumer)}.
     *
     * @param key key to query, format {@code pluginid:subkey}
     * @return value associated with the key or null if none
     */
    @Nullable
    Object get(@NotNull String key);

    /**
     * Request being notified when a key/value becomes available.
     *
     * <p>This is primarily intended to resolve load order issues, when there is no good time to call {@link #get(String)}.
     *
     * <p>If there is already a value associated with the {@code key}, the consumer will be invoked directly, otherwise
     * when one of the {@code put} methods adds a value for the key. The invocation happens on the thread calling
     * {@link #whenAvailable} or on whichever thread calls {@code put} with the same {@code key}.
     *
     * <p>The request will only act once, not if the value changes again.
     *
     * <p>Example use:
     * <pre>
     * CoproliteLoader.getInstance().getObjectShare().whenAvailable("some_plugin:someValue", (k, v) -> {
     *   if (v instanceof String value) {
     *     // use value
     *   }
     * });
     * </pre>
     *
     * @param key key to react upon, format {@code pluginid:subkey}
     * @param consumer consumer receiving the key/value pair: key first, value second
     */
    void whenAvailable(@NotNull String key, @NotNull BiConsumer<String, Object> consumer);

    /**
     * Request being notified when a key/value becomes available.
     *
     * <p>This is primarily intended to resolve load order issues, when there is no good time to call {@link #get(String)}.
     *
     * @param key key to react upon, format {@code pluginid:subkey}
     * @param consumer consumer receiving the value
     */
    void whenAvailable(@NotNull String key, @NotNull Consumer<Object> consumer);

    /**
     * Set the value for a specific key.
     *
     * @param key key to add a value for, format {@code pluginid:subkey}
     * @param value value to add, must not be null
     * @return previous value associated with the key, null if none
     */
    @Nullable
    Object put(@NotNull String key, @NotNull Object value);

    /**
     * Set the value for a specific key if there isn't one yet.
     *
     * <p>This is an atomic operation, thus thread safe contrary to using get+put.
     *
     * @param key key to add a value for, format {@code pluginid:subkey}
     * @param value value to add, must not be null
     * @return previous value associated with the key, null if none and thus the entry changed
     */
    @Nullable
    Object putIfAbsent(@NotNull String key, @NotNull Object value);

    /**
     * Remove the value for a specific key.
     *
     * @param key key to remove the value for, format {@code pluginid:subkey}
     * @return previous value associated with the key, null if none
     */
    @Nullable
    Object remove(@NotNull String key);
}
