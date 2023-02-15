/*
 * Copyright 2023 DADi590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dadi590.assist_c_a.GlobalUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 */
public final class UtilsReflection {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsReflection() {
	}

	/**
	 * <p>Gets a method from a class ready to be executed with parameters.</p>
	 * <p>Use with {@link #invokeMethod(Method, Object, Object...)}.</p>
	 *
	 * @param cls the class to get the method from
	 * @param method_name the name of the method to get
	 * @param parameter_types the method parameter types, or null if it's a no-arg method
	 *
	 * @return the method, or null in case it's not found
	 */
	@Nullable
	public static Method getMethod(@NonNull final Class<?> cls, @NonNull final String method_name,
								   @Nullable final Class<?>... parameter_types) {
		try {
			return cls.getDeclaredMethod(method_name, parameter_types);
		} catch (final NoSuchMethodException ignored) {
			return null;
		}
	}

	/**
	 * <p>Invokes the given method with the given parameters.</p>
	 *
	 * @param method the method to invoke
	 * @param invoke_on the object to invoke the method on, or null to not invoke in any object
	 * @param parameters the parameters to give
	 *
	 * @return an instance of {@link InvokeMethodObj}
	 */
	@NonNull
	public static InvokeMethodObj invokeMethod(@NonNull final Method method, @Nullable final Object invoke_on,
											   @Nullable final Object... parameters) {
		try {
			final boolean is_accessible = method.isAccessible();
			try {
				method.setAccessible(true);
			} catch (final SecurityException ignored) {
				// Ignore the exception. If the method can't be accessed or not, is the same as if it executed
				// correctly or not and then the null return will happen.
			}
			final InvokeMethodObj invokeMethodObj = new InvokeMethodObj(method.invoke(invoke_on, parameters), true);
			try {
				method.setAccessible(is_accessible);
			} catch (final SecurityException ignored) {
				// Ignore the exception. We already executed the method, that's all that matters.
			}

			return invokeMethodObj;
		} catch (final Exception ignored) {
		}

		return new InvokeMethodObj(null, false);
	}
	/**
	 * <p>Class to use for the return value of {@link #invokeMethod(Method, Object, Object...)}.</p>
	 * <p>Always check the {@code success} variable. If the invocation is not successful, the null will be returned,
	 * like it would if the actual return value of the invoked method were null.</p>
	 * <p>Read the documentation of the class constructor to know more about it.</p>
	 */
	public static final class InvokeMethodObj {
		/** The returning value from the method. */
		public final Object ret_var;
		/** True in case the method invocation was successful, false otherwise, and therefore the return value should be
		 * ignored. */
		public final boolean success;

		/**
		 * .
		 * @param ret_var {@link #ret_var}
		 * @param success {@link #success}
		 */
		InvokeMethodObj(@Nullable final Object ret_var, final boolean success) {
			this.ret_var = ret_var;
			this.success = success;
		}
	}

	/**
	 * <p>Checks if a field is present on an object.</p>
	 *
	 * @param obj the object to search on
	 * @param field_name the name of the field
	 *
	 * @return true if it's found, false otherwise
	 */
	public static boolean isFieldDeclared(@NonNull final Object obj, @NonNull final String field_name) {
		try {
			obj.getClass().getDeclaredField(field_name);

			return true;
		} catch (final NoSuchFieldException ignored) {
		}

		return false;
	}

	/**
	 * <p>Gets the value of a field from an object.</p>
	 *
	 * @param obj the object to get the value from
	 * @param field_name the name of the field
	 *
	 * @return the value if - ATTENTION: if the field is not found, this will return null, which it can return anyway
	 * as the value of a field, so check first if the field exists with {@link #isFieldDeclared(Object, String)}
	 */
	@Nullable
	public static Object getFieldValue(@NonNull final Object obj, @NonNull final String field_name) {
		final Field field;
		try {
			field = obj.getClass().getDeclaredField(field_name);
		} catch (final NoSuchFieldException ignored) {
			return null;
		}

		field.setAccessible(true);
		try {
			return field.get(obj);
		} catch (final IllegalAccessException ignored) {
			return null;
		}
	}
}
