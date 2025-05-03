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

package com.edw590.visor_c_a.OpenGL;

public class Vertex {
	public Vector position = null;
	public Vector color = null;

	public Vertex(final float x, final float y, final float z) {
		this.position = new Vector(x, y, z);
		this.color = new Vector(1.0f, 1.0f, 1.0f);
	}

	public Vertex(final float x, final float y, final float z, final float r, final float g, final float b,
				  final float a) {
		position = new Vector(x, y, z);
		color = new Vector(r, g, b, a);
	}
}
