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

import me.nelonn.coprolite.launcher.api.GamePatch;
import me.nelonn.coprolite.loader.api.CoproliteLauncher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Consumer;
import java.util.function.Function;

public class PaperPluginPatch extends GamePatch {
    @Override
    public void process(CoproliteLauncher launcher, Function<String, ClassReader> classSource, Consumer<ClassNode> classEmitter) {
        /*String pluginClassName = "org.bukkit.plugin.java.JavaPlugin";
        try {
            ClassNode pluginClass = readClass(classSource.apply(pluginClassName));
            classEmitter.accept(pluginClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        /*ClassNode pluginClass = readClass(classSource.apply(pluginClassName));
        if (pluginClass == null) {
            throw new RuntimeException("Could not load plugin class " + pluginClassName + "!");
        }

        for (MethodNode method : pluginClass.methods) {
            if ("<init>".equals(method.name) && "()V".equals(method.desc)) {
                InsnList instructions = method.instructions;

                int start = 0;

                for (AbstractInsnNode instruction : instructions) {
                    if (start > 0) {
                        if (start > 1) {
                            if (instruction.getOpcode() == Opcodes.ATHROW) {
                                start = 0;
                                instructions.set(instruction, new InsnNode(Opcodes.RETURN));
                            } else {
                                instructions.remove(instruction);
                            }
                        } else {
                            start = 2;
                        }
                    }
                    if (instruction.getType() == AbstractInsnNode.LINE) {
                        LineNumberNode lineNumberNode = (LineNumberNode) instruction;
                        if (lineNumberNode.line == 56) {
                            start = 1;
                        }
                    }
                }
            }
        }

        classEmitter.accept(pluginClass);*/
    }
}
