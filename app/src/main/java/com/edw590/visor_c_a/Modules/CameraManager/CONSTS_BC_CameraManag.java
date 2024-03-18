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

package com.edw590.visor_c_a.Modules.CameraManager;

/**
 * <p>Actions, extras, and classes to use to send a broadcast to this module.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
public final class CONSTS_BC_CameraManag {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_BC_CameraManag() {
	}

	/**
	 * <p>Explanation: same as the main executed function.</p>
	 * <p>Main executed function: {@link CameraManagement#useCamera(int)}.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsCameraManagerBC}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras (ordered parameters):</p>
	 * <p>- {@link #EXTRA_USE_CAMERA_1}: mandatory</p>
	 */
	static final String ACTION_USE_CAMERA = "CameraManager_ACTION_USE_CAMERA";
	static final String EXTRA_USE_CAMERA_1 = "CameraManager_EXTRA_USE_CAMERA_1";


	/**
	 * <p>Explanation: warns when a picture was taken.</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_PICTURE_TAKEN = "CameraManager_ACTION_PICTURE_TAKEN";
	/**
	 * <p>Explanation: warns when a picture was taken, but it was not possible to focus the camera (not because the
	 * camera hardware doesn't support it).</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_PICTURE_TAKEN_NO_FOCUS = "CameraManager_ACTION_PICTURE_TAKEN_NO_FOCUS";


	/**
	 * <p>Explanation: warns that the camera could not be opened.</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_ERR_CANT_OPEN_CAM = "CameraManager_ACTION_ERR_CANT_OPEN_CAM";
	/**
	 * <p>Explanation: warns that it's not possible to create the picture file.</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_ERR_CANT_CREATE_FILE = "CameraManager_ACTION_ERR_CANT_CREATE_FILE";
	/**
	 * <p>Explanation: warns that the picture file that had been created was just deleted.</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_ERR_FILE_DELETED = "CameraManager_ACTION_ERR_FILE_DELETED";
	/**
	 * <p>Explanation: warns that an error occurred while writing to the file (excluding the possibility of it having
	 * been deleted - that's with {@link #ACTION_ERR_FILE_DELETED}).</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_ERR_WRITING_PIC_TO_FILE = "CameraManager_ACTION_ERR_WRITING_PIC_TO_FILE";
	/**
	 * <p>Explanation: warns that the chosen flash mode is not available for the device camera.</p>
	 * <p>Is broadcast by the class(es): {@link TakePictureOld}.</p>
	 * <p>To be received only by the class(es): {@link CameraManagement}.</p>
	 * <p>Extras: none.</p>
	 */
	static final String ACTION_ERR_UNSUPPORTED_FLASH_MODE = "CameraManager_ACTION_ERR_UNSUPPORTED_FLASH_MODE";
}
