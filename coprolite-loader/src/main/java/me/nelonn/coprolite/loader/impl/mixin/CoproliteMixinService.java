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

import me.nelonn.coprolite.api.CoproliteLoader;
import me.nelonn.coprolite.loader.api.CoproliteLauncher;
import me.nelonn.coprolite.loader.impl.plugin.PluginContainerImpl;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.transformers.MixinClassReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class CoproliteMixinService extends MixinServiceAbstract implements IClassProvider, IClassBytecodeProvider {
    private final ClassLoader loader = getClass().getClassLoader();
    private final static byte[] refMap = "{\"mappings\":{}}".getBytes(StandardCharsets.UTF_8);
    private static IMixinTransformer transformer;

    public static IMixinTransformer getTransformer() {
        return transformer;
    }

    @Override
    public String getName() {
        return "Coprolite";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_21;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList(CoproliteMixinPlatformAgent.class.getName());
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleVirtual(getName());
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (name.equals("mixin.refmap.json")) return new ByteArrayInputStream(refMap);
        try {
            String[] names = name.split("\\|", 2);
            if (names.length == 2) {
                JarFile jar = CoproliteLoader.getInstance().getPluginContainer(names[0])
                        .map(it -> ((PluginContainerImpl) it).getJarFile())
                        .orElseThrow(() -> new NoSuchFileException("No such plugin: " + names[0]));
                JarEntry entry = jar.getJarEntry(names[1]);
                if (entry == null) throw new NoSuchFileException("No such file: " + names[1]);
                return jar.getInputStream(entry);
            }
            InputStream is = CoproliteLauncher.getInstance().getResourceAsStream(name);
            if (is == null) throw new NoSuchFileException("No such file: " + name);
            return is;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected ILogger createLogger(String name) {
        return MixinLogger.get(name);
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory && transformer == null) {
            transformer = ((IMixinTransformerFactory) internal).createTransformer();
        }
        super.offer(internal);
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, loader);
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, loader);
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException {
        return this.getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException {
        if (!runTransformers) {
            throw new IllegalArgumentException("ModLauncher service does not currently support retrieval of untransformed bytecode");
        }

        String canonicalName = name.replace('/', '.');
        try {
            byte[] classBytes = CoproliteLauncher.getInstance().getClassByteArray(name, runTransformers);
            if (classBytes.length != 0) {
                ClassNode classNode = new ClassNode();
                ClassReader classReader = new MixinClassReader(classBytes, canonicalName);
                classReader.accept(classNode, readerFlags);
                return classNode;
            }
        } catch (IOException e) {
            throw new ClassNotFoundException(canonicalName, e);
        }

        throw new ClassNotFoundException(canonicalName);
    }
}