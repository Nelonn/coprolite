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

import java.util.Map;

/**
 * Represents a custom value in the {@code coprolite.plugin.json}.
 */
public interface CustomValue {
	/**
	 * Returns the type of the value.
	 */
	@NotNull
	CvType getType();

	/**
	 * Returns this value as an {@link CvType#OBJECT}.
	 *
	 * @return this value
	 * @throws ClassCastException if this value is not an object
	 */
	@NotNull
	CvObject getAsObject();

	/**
	 * Returns this value as an {@link CvType#ARRAY}.
	 *
	 * @return this value
	 * @throws ClassCastException if this value is not an array
	 */
	@NotNull
	CvArray getAsArray();

	/**
	 * Returns this value as a {@link CvType#STRING}.
	 *
	 * @return this value
	 * @throws ClassCastException if this value is not a string
	 */
	@NotNull
	String getAsString();

	/**
	 * Returns this value as a {@link CvType#NUMBER}.
	 *
	 * @return this value
	 * @throws ClassCastException if this value is not a number
	 */
	@NotNull
	Number getAsNumber();

	/**
	 * Returns this value as a {@link CvType#BOOLEAN}.
	 *
	 * @return this value
	 * @throws ClassCastException if this value is not a boolean
	 */
	boolean getAsBoolean();

	/**
	 * Represents an {@link CvType#OBJECT} value.
	 */
	interface CvObject extends CustomValue, Iterable<Map.Entry<String, CustomValue>> {
		/**
		 * Returns the number of key-value pairs within this object value.
		 */
		int size();

		/**
		 * Returns whether a {@code key} is present within this object value.
		 *
		 * @param key the key to check
		 * @return whether the key is present
		 */
		boolean containsKey(@NotNull String key);

		/**
		 * Gets the value associated with a {@code key} within this object value.
		 *
		 * @param key the key to check
		 * @return the value associated, or {@code null} if no such value is present
		 */
		@Nullable
		CustomValue get(@NotNull String key);
	}

	/**
	 * Represents an {@link CvType#ARRAY} value.
	 */
	interface CvArray extends CustomValue, Iterable<CustomValue> {
		/**
		 * Returns the number of values within this array value.
		 */
		int size();

		/**
		 * Gets the value at {@code index} within this array value.
		 *
		 * @param index the index of the value
		 * @return the value associated
		 * @throws IndexOutOfBoundsException if the index is not within {{@link #size()}}
		 */
		@NotNull
		CustomValue get(int index);
	}

	/**
	 * The possible types of a custom value.
	 */
	enum CvType {
		OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL;
	}
}