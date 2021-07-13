package com.dadi590.assist_c_a.GlobalUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.External.ExecuteAsRootBase;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utility class with functions that use directly a shell.</p>
 */
public final class UtilsShell {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsShell() {
	}

	/**
	 * <p>Gets the access rights of a file or folder.</p>
	 *
	 * @param path the path to the file or folder
	 * @param human_readable true to return "drwxr-xr-x", for example; false to return in octal form (for example, 755)
	 *
	 * @return one of the strings mentioned in {@code human_readable} parameter
	 */
	@NonNull
	public static String getAccessRights(@NonNull final String path, final boolean human_readable) {
		final String parameter;
		if (human_readable) {
			parameter = "%A";
		} else {
			parameter = "%a";
		}

		final List<String> commands = new ArrayList<>(2);
		commands.add("su");
		commands.add("stat -c " + parameter + " " + path);

		ExecuteAsRootBase.CmdOuputObj commands_output = ExecuteAsRootBase.executeShellCmd(commands);
		if (commands_output == null) {
			commands.remove(0);
			commands_output = ExecuteAsRootBase.executeShellCmd(commands);
		}

		assert commands_output != null; // Just want the warning out. It won't be null if the "su" command is not there.
		return UtilsGeneral.convertBytes2Printable(commands_output.output_stream);
	}

	/*public static boolean createFile(@NonNull final String complete_name) {
		final String partition = complete_name.split("/")[1];
	}*/
}
