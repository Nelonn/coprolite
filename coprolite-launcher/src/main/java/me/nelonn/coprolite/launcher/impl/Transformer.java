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

import me.nelonn.coprolite.loader.api.CoproliteLauncher;
import me.nelonn.coprolite.loader.impl.CoproliteMixinBootstrap;
import me.nelonn.coprolite.loader.impl.CoproliteTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public final class Transformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] bytes) {
            if (!CoproliteMixinBootstrap.isMixinReady()) return bytes;
            GameTransformer transformer = ((Launcher) CoproliteLauncher.getInstance()).getProvider().getTransformer();
            if (transformer != null) {
                byte[] patched = transformer.transform(className.replace('/', '.'));
                if (patched != null) {
                    bytes = patched;
                }
            }
            return CoproliteTransformer.transform(loader, className, classBeingRedefined, protectionDomain, bytes);
        }
    }