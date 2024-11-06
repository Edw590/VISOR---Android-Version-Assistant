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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import GPTComm.GPTComm;
import UtilsSWA.UtilsSWA;

/**
 * <p>Fragment that shows the status of each module of the assistant.</p>
 */
public final class TabCommunicatorMemories extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		AppCompatTextView txtView_info = new AppCompatTextView(requireContext());
		txtView_info.setText("List of memories stored for the smart LLM, one per line (use in landscape mode):");

		AppCompatEditText editTxt_memories_text = new AppCompatEditText(requireContext());
		editTxt_memories_text.setSingleLine(false);
		editTxt_memories_text.setHint("Stored memories on the smart LLM");
		if (UtilsSWA.isCommunicatorConnectedSERVER()) {
			editTxt_memories_text.setText(GPTComm.getMemories());
		}

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save memories");
		btn_save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				GPTComm.setMemories(editTxt_memories_text.getText().toString());
			}
		});

		linearLayout.addView(txtView_info);
		linearLayout.addView(btn_save);
		linearLayout.addView(editTxt_memories_text);
	}
}
