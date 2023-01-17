/*
 * Copyright 2022 DADi590
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

package com.dadi590.assist_c_a.ActivitiesFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsList;
import com.dadi590.assist_c_a.R;

/**
 * <p>Fragment that shows the status of each module of the assistant.</p>
 */
public class FragCmdsList extends Fragment {

	@Nullable
	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_cmds_list, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final LinearLayout linearLayout = view.findViewById(R.id.frag_cmds_list_linear_layout);

		for (final String command_desc : CmdsList.CMDS_LIST_description) {
			final TextView textView = new TextView(requireContext());
			textView.setTextIsSelectable(true);
			textView.setText("--> " + command_desc);

			linearLayout.addView(textView);
		}
	}
}
