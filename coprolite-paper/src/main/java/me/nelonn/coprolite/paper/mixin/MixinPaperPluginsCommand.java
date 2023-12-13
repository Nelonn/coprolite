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

import com.google.common.collect.Lists;
import io.papermc.paper.command.PaperPluginsCommand;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.type.paper.PaperPluginParent;
import io.papermc.paper.plugin.provider.type.spigot.SpigotPluginProvider;
import me.nelonn.coprolite.api.CoproliteLoader;
import me.nelonn.coprolite.api.PluginContainer;
import me.nelonn.coprolite.api.PluginMetadata;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Mixin(PaperPluginsCommand.class)
public abstract class MixinPaperPluginsCommand {
    @Unique
    private static final Component COPROLITE_HEADER = Component.text("Coprolite Plugins:", TextColor.color(153, 51, 255));

    @Shadow @Final private static Component PAPER_HEADER;
    @Shadow @Final private static Component BUKKIT_HEADER;
    @Shadow @Final private static Component PLUGIN_TICK;
    @Shadow @Final private static Component PLUGIN_TICK_EMPTY;

    @Shadow
    private static <T> List<Component> formatProviders(TreeMap<String, PluginProvider<T>> plugins) {
        return null;
    }

    @Unique
    private static List<Component> formatPlugins(TreeMap<String, PluginMetadata> plugins) {
        List<Component> components = new ArrayList<>(plugins.size());
        for (PluginMetadata metadata : plugins.values()) {
            components.add(Component.text(metadata.getName() != null ? metadata.getName() : metadata.getId(),
                    NamedTextColor.GREEN));
        }

        boolean isFirst = true;
        List<Component> formattedSublists = new ArrayList<>();
        /*
        Split up the plugin list for each 10 plugins to get size down

        Plugin List:
        - Plugin 1, Plugin 2, .... Plugin 10,
          Plugin 11, Plugin 12 ... Plugin 20,
         */
        for (List<Component> componentSublist : Lists.partition(components, 10)) {
            Component component = Component.space();
            if (isFirst) {
                component = component.append(PLUGIN_TICK);
                isFirst = false;
            } else {
                component = PLUGIN_TICK_EMPTY;
                //formattedSublists.add(Component.empty()); // Add an empty line, the auto chat wrapping and this makes it quite jarring.
            }

            formattedSublists.add(component.append(Component.join(JoinConfiguration.commas(true), componentSublist)));
        }

        return formattedSublists;
    }

    /**
     * @author Nelonn
     * @reason show coprolite paper plugins in console
     */
    @Overwrite
    public boolean execute(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        if (!((Command) (Object) this).testPermission(sender)) return true;

        // Coprolite start
        TreeMap<String, PluginMetadata> coprolitePlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (PluginContainer plugin : CoproliteLoader.getInstance().getAllPlugins()) {
            coprolitePlugins.put(plugin.getMetadata().getName(), plugin.getMetadata());
        }
        // Coprolite end
        TreeMap<String, PluginProvider<JavaPlugin>> paperPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        TreeMap<String, PluginProvider<JavaPlugin>> spigotPlugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


        for (PluginProvider<JavaPlugin> provider : LaunchEntryPointHandler.INSTANCE.get(Entrypoint.PLUGIN).getRegisteredProviders()) {
            PluginMeta configuration = provider.getMeta();

            if (provider instanceof SpigotPluginProvider) {
                spigotPlugins.put(configuration.getDisplayName(), provider);
            } else if (provider instanceof PaperPluginParent.PaperServerPluginProvider) {
                paperPlugins.put(configuration.getDisplayName(), provider);
            }
        }

        Component infoMessage = Component.text("Server Plugins (%s):".formatted(coprolitePlugins.size() + paperPlugins.size() + spigotPlugins.size()), NamedTextColor.WHITE); // Coprolite
        //.append(INFO_ICON_START.hoverEvent(SERVER_PLUGIN_INFO)); TODO: Add docs

        sender.sendMessage(infoMessage);

        // Coprolite start
        if (!coprolitePlugins.isEmpty()) {
            sender.sendMessage(COPROLITE_HEADER);
        }

        for (Component component : formatPlugins(coprolitePlugins)) {
            sender.sendMessage(component);
        }

        // Coprolite end
        if (!paperPlugins.isEmpty()) {
            sender.sendMessage(PAPER_HEADER);
        }

        for (Component component : formatProviders(paperPlugins)) {
            sender.sendMessage(component);
        }

        if (!spigotPlugins.isEmpty()) {
            sender.sendMessage(BUKKIT_HEADER);
        }

        for (Component component : formatProviders(spigotPlugins)) {
            sender.sendMessage(component);
        }

        return true;
    }
}
