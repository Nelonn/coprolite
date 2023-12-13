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

package me.nelonn.coprolite.loader.impl.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import me.nelonn.coprolite.api.CustomValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class CustomValueImpl implements CustomValue {

    @NotNull
    public static CustomValue read(@NotNull JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new StringImpl(primitive.getAsString());
            } else if (primitive.isNumber()) {
                return new NumberImpl(primitive.getAsNumber());
            } else if (primitive.isBoolean()) {
                return BooleanImpl.of(primitive.getAsBoolean());
            }
        } else if (element.isJsonArray()) {
            final List<CustomValue> elements = new ArrayList<>();
            for (JsonElement it : element.getAsJsonArray()) {
                elements.add(read(it));
            }
            return new ArrayImpl(elements);
        } else if (element.isJsonObject()) {
            final Map<String, CustomValue> members = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> it : element.getAsJsonObject().entrySet()) {
                members.put(it.getKey(), read(it.getValue()));
            }
            return new ObjectImpl(members);
        } else if (element.isJsonNull()) {
            return NullImpl.NULL;
        }
        throw new JsonParseException("Unknown element type");
    }

    @Override
    @NotNull
    public final CvObject getAsObject() {
        if (this instanceof ObjectImpl) {
            return (ObjectImpl) this;
        } else {
            throw new ClassCastException("can't convert "+getType().name()+" to Object");
        }
    }

    @Override
    @NotNull
    public final CvArray getAsArray() {
        if (this instanceof ArrayImpl) {
            return (ArrayImpl) this;
        } else {
            throw new ClassCastException("can't convert "+getType().name()+" to Array");
        }
    }

    @Override
    @NotNull
    public final String getAsString() {
        if (this instanceof StringImpl) {
            return ((StringImpl) this).value;
        } else {
            throw new ClassCastException("can't convert "+getType().name()+" to String");
        }
    }

    @Override
    @NotNull
    public final Number getAsNumber() {
        if (this instanceof NumberImpl) {
            return ((NumberImpl) this).value;
        } else {
            throw new ClassCastException("can't convert "+getType().name()+" to Number");
        }
    }

    @Override
    public final boolean getAsBoolean() {
        if (this instanceof BooleanImpl) {
            return ((BooleanImpl) this).value;
        } else {
            throw new ClassCastException("can't convert "+getType().name()+" to Boolean");
        }
    }

    public static final class ObjectImpl extends CustomValueImpl implements CvObject {
        private final Map<String, CustomValue> members;

        public ObjectImpl(@NotNull Map<String, CustomValue> members) {
            this.members = Collections.unmodifiableMap(members);
        }

        @Override
        @NotNull
        public CvType getType() {
            return CvType.OBJECT;
        }

        @Override
        public int size() {
            return this.members.size();
        }

        @Override
        public boolean containsKey(@NotNull String key) {
            return this.members.containsKey(key);
        }

        @Override
        public CustomValue get(@NotNull String key) {
            return this.members.get(key);
        }

        @Override
        @NotNull
        public Iterator<Map.Entry<String, CustomValue>> iterator() {
            return this.members.entrySet().iterator();
        }
    }

    public static final class ArrayImpl extends CustomValueImpl implements CvArray {
        private final List<CustomValue> entries;

        public ArrayImpl(@NotNull List<CustomValue> entries) {
            this.entries = Collections.unmodifiableList(entries);
        }

        @Override
        @NotNull
        public CvType getType() {
            return CvType.ARRAY;
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public @NotNull CustomValue get(int index) {
            return entries.get(index);
        }

        @Override
        @NotNull
        public Iterator<CustomValue> iterator() {
            return entries.iterator();
        }
    }

    public static final class StringImpl extends CustomValueImpl {
        private final String value;

        public StringImpl(@NotNull String value) {
            this.value = value;
        }

        @Override
        public @NotNull CvType getType() {
            return CvType.STRING;
        }
    }

    public static final class NumberImpl extends CustomValueImpl {
        private final Number value;

        NumberImpl(@NotNull Number value) {
            this.value = value;
        }

        @Override
        @NotNull
        public CvType getType() {
            return CvType.NUMBER;
        }
    }

    public static final class BooleanImpl extends CustomValueImpl {
        public static final BooleanImpl TRUE = new BooleanImpl(true);
        public static final BooleanImpl FALSE = new BooleanImpl(false);

        public static BooleanImpl of(boolean bool) {
            return bool ? TRUE : FALSE;
        }

        private final boolean value;

        private BooleanImpl(boolean value) {
            this.value = value;
        }

        @Override
        @NotNull
        public CvType getType() {
            return CvType.BOOLEAN;
        }
    }

    public static final class NullImpl extends CustomValueImpl {
        static final NullImpl NULL = new NullImpl();

        private NullImpl() {
        }

        @Override
        @NotNull
        public CvType getType() {
            return CvType.NULL;
        }
    }
}
