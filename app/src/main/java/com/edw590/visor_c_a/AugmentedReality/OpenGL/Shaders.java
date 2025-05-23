/*
 * Copyright 2021-2025 Edw590
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

package com.edw590.visor_c_a.AugmentedReality.OpenGL;

public final class Shaders {

	static final String VERTEX_SHADER_CODE =
					"attribute vec3 a_position;" +
					"attribute vec4 a_color;" +
					"varying vec4 v_color;" +
					"uniform mat4 u_model;" +
					"uniform mat4 u_view;" +
					"uniform mat4 u_projection;" +
					"void main() {" +
						// Multiply the position first by the rotation matrix, then by the translation matrix and
						// finally by the projection matrix: projection * view * model * position.
					"    gl_Position = u_projection * u_view * u_model * vec4(a_position, 1.0);" +
					"    v_color = a_color;" +
					"}";

	static final String FRAGMENT_SHADER_CODE =
					"#version 100\n" +
					"precision mediump float;" +
					"varying vec4 v_color;" +
					"void main() {" +
					"    gl_FragColor = v_color;" +
					"}";
}
