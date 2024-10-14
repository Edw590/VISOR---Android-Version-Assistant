package com.edw590.visor_c_a.Modules.TasksExecutor;

import android.Manifest;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;

public class TasksExecutor implements IModuleInst {

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return true;
	}
	@Override
	public void destroy() {
		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		final String[] min_required_permissions = {
				Manifest.permission.RECORD_AUDIO,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
		};
		return UtilsPermsAuths.checkSelfPermissions(min_required_permissions)
				&& UtilsCheckHardwareFeatures.isMicrophoneSupported();
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	public TasksExecutor() {
	}
}
