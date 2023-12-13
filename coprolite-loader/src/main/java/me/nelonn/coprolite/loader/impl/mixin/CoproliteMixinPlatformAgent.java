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

package me.nelonn.coprolite.loader.impl.mixin;

import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

import java.util.Collection;

@SuppressWarnings("unused")
public final class CoproliteMixinPlatformAgent extends MixinPlatformAgentDefault implements IMixinPlatformServiceAgent {
    @Override
    public void init() {
    }

    @Override
    public String getSideName() {
        return Constants.SIDE_DEDICATEDSERVER;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }
}