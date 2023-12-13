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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.security.ProtectionDomain;

public class CoproliteTransformer {

    public static byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain, byte[] bytes) {
        /*IMixinTransformer transformer = CoproliteMixinService.getTransformer();
        return transformer == null ? null : transformer.transformClass(MixinEnvironment.getDefaultEnvironment(),
                className.replace('/', '.'), relocate(bytes));*/

        IMixinTransformer transformer = CoproliteMixinService.getTransformer();

        if (transformer == null) {
            return null;
        }

        if (transformer != null) {
            //bytes = transformer.transformClass(MixinEnvironment.getDefaultEnvironment(), className.replace('/', '.'), relocate(bytes));
            bytes = transformer.transformClass(MixinEnvironment.getDefaultEnvironment(), className.replace('/', '.'), relocate(bytes));
        }

        return bytes;

        /*boolean applyAccessWidener = CoproliteLoaderImpl.INSTANCE.getAccessWidener().getTargets().contains(className);

        if (!applyAccessWidener) return bytes;

        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        ClassVisitor visitor = classWriter;
        int visitorCount = 0;

        //classReader.accept(new ClassRemapper(CoproliteLoaderImpl.ASM_VERSION, classWriter, remapper) { }, ClassReader.EXPAND_FRAMES);

        if (applyAccessWidener) {
            visitor = AccessWidenerClassVisitor.createClassVisitor(CoproliteLoaderImpl.ASM_VERSION, classWriter, CoproliteLoaderImpl.INSTANCE.getAccessWidener());
            visitorCount++;
        }

        if (visitorCount <= 0) {
            return bytes;
        }

        classReader.accept(visitor, 0);
        return classWriter.toByteArray();*/
    }

    private final static Remapper remapper = new Remapper() {
        /*@Override
        public String map(String name) {
            if (!name.startsWith("org/bukkit/craftbukkit/Main") && !name.startsWith("org/bukkit/craftbukkit/libs/") &&
                    name.startsWith("org/bukkit/craftbukkit/") && !name.startsWith("org/bukkit/craftbukkit/v1_")) {
                if (obcVersion == null) throw new IllegalStateException("Cannot detect minecraft version!");
                return obcClassName + name.substring("org/bukkit/craftbukkit/".length());
            }
            return super.map(name);
        }*/
    };

    private static byte[] relocate(byte[] data) {
        ClassReader cr = new ClassReader(data);
        ClassWriter cw = new ClassWriter(0);
        cr.accept(new ClassRemapper(Opcodes.ASM9, cw, remapper) {
        }, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

}
