/*
 * Copyright 2021-2024 Edw590
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

package com.edw590.visor_c_a.ActivitiesFragments.Tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;
import com.edw590.visor_c_a.R;

public final class TabCommunicatorCmdsList extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		StringBuilder commands_desc = new StringBuilder(500);
		for (final String command_desc : CmdsList.CMDS_LIST_description) {
			commands_desc.append("--> ").append(command_desc).append("\n");
		}

		AppCompatTextView txtView = new AppCompatTextView(requireContext());
		txtView.setText("List of all commands and variations available (optional words in [...] and generic " +
				"descriptions in (...)):\n\n" +
				"" +
				"(Note: there is more than one way to say a command, with synonyms and random words in between " +
				"('switch on the phone's wifi', 'what's the current time', 'terminate the phone call').)\n\n" +
				"" +
				commands_desc);

		linearLayout.addView(txtView);
	}
}
