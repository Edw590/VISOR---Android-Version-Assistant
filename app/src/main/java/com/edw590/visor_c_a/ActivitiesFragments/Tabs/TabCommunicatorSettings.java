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
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import java.util.ArrayList;
import java.util.List;

import GPTComm.GPTComm;
import SettingsSync.SettingsSync;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabCommunicatorSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabCommunicatorSettings extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ModsFileInfo.Mod7UserInfo mod_7_user_info = SettingsSync.getMod7InfoUSERSETS();

		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		AppCompatEditText editTxt_server_url = new AppCompatEditText(requireContext());
		editTxt_server_url.setHint("Ollama URL for use in the server (example: localhost:11434)");
		editTxt_server_url.setText(mod_7_user_info.getServer_url());
		editTxt_server_url.setSingleLine();

		AppCompatEditText editTxt_user_nickname = new AppCompatEditText(requireContext());
		editTxt_user_nickname.setHint("User nickname (Sir, for example)");
		editTxt_user_nickname.setText(mod_7_user_info.getUser_nickname());
		editTxt_user_nickname.setSingleLine();

		SwitchCompat switch_prioritize_clients = new SwitchCompat(requireContext());
		switch_prioritize_clients.setText("Give priority to models on the clients?");
		switch_prioritize_clients.setChecked(mod_7_user_info.getPrioritize_clients_models());

		AppCompatEditText editTxt_models_priorities = new AppCompatEditText(requireContext());
		editTxt_models_priorities.setHint("Models to use, one per line by order of priority (add them first below)");
		editTxt_models_priorities.setText(mod_7_user_info.getModel_priorities());

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			mod_7_user_info.setServer_url(editTxt_server_url.getText().toString());
			mod_7_user_info.setUser_nickname(editTxt_user_nickname.getText().toString());
			mod_7_user_info.setPrioritize_clients_models(switch_prioritize_clients.isChecked());
			mod_7_user_info.setModel_priorities(editTxt_models_priorities.getText().toString());
		});

		AppCompatEditText editTxt_new_model_name = new AppCompatEditText(requireContext());
		editTxt_new_model_name.setHint("New model name (example: llama3.2:latest)");
		editTxt_new_model_name.setSingleLine();

		AppCompatButton btn_add_model = new AppCompatButton(requireContext());
		btn_add_model.setText("Add model");
		btn_add_model.setOnClickListener(v -> {
			String model_name = editTxt_new_model_name.getText().toString();
			if (model_name.isEmpty()) {
				Utils.createErrorDialog(requireContext(), "The model name must not be empty");

				return;
			}

			GPTComm.addUpdateModelOLLAMA(model_name, GPTComm.MODEL_TYPE_TEXT, false, 4096, 0.8f, "");

			mod_7_user_info.setModel_priorities(mod_7_user_info.getModel_priorities() + "\n" + model_name);

			Utils.reloadFragment(this);
		});

		ExpandableListView expandable_list_view = new ExpandableListView(requireContext());
		GenericExpandableListAdapter adapter = new GenericExpandableListAdapter(requireContext());
		expandable_list_view.setAdapter(adapter);
		expandable_list_view.setLayoutParams(linearLayout.getLayoutParams());
		expandable_list_view.setOnGroupCollapseListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view, false);
		});
		expandable_list_view.setOnGroupExpandListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view, false);
		});

		String models_priorities = mod_7_user_info.getModel_priorities();
		if (!models_priorities.isEmpty()) {
			for (final String model_name : models_priorities.split("\n")) {
				if (model_name.isEmpty()) {
					continue;
				}

				adapter.addItem(model_name, createModelSetter(model_name));
			}
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view, false);

		linearLayout.addView(editTxt_server_url);
		linearLayout.addView(editTxt_user_nickname);
		linearLayout.addView(switch_prioritize_clients);
		linearLayout.addView(editTxt_models_priorities);
		linearLayout.addView(btn_save);
		linearLayout.addView(editTxt_new_model_name);
		linearLayout.addView(btn_add_model);
		linearLayout.addView(expandable_list_view);
	}

	private List<View> createModelSetter(final String model_name) {
		List<View> child_views = new ArrayList<>(10);

		ModsFileInfo.Model model_info = GPTComm.getModelOLLAMA(model_name);
		if (model_info == null) {
			AppCompatTextView txtView_model_not_found = new AppCompatTextView(requireContext());
			txtView_model_not_found.setText("Model not found");
			child_views.add(txtView_model_not_found);

			return child_views;
		}

		AppCompatTextView txtView_model_name = new AppCompatTextView(requireContext());
		txtView_model_name.setText("Model name: " + model_name);

		AppCompatSpinner spinner_model_type = new AppCompatSpinner(requireContext());
		spinner_model_type.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
				new String[]{"Text", "Text+Vision"}));
		switch (model_info.getType()) {
			case GPTComm.MODEL_TYPE_TEXT: {
				spinner_model_type.setSelection(0);

				break;
			}
			case GPTComm.MODEL_TYPE_VISION: {
				spinner_model_type.setSelection(1);

				break;
			}
		}

		SwitchCompat switch_model_has_tool_role = new SwitchCompat(requireContext());
		switch_model_has_tool_role.setText("Is the tool role available for the model?");
		switch_model_has_tool_role.setChecked(model_info.getHas_tool_role());

		AppCompatEditText editTxt_ctx_size = new AppCompatEditText(requireContext());
		editTxt_ctx_size.setHint("Context size (example: 4096)");
		editTxt_ctx_size.setText(Integer.toString(model_info.getContext_size()));
		editTxt_ctx_size.setSingleLine();
		editTxt_ctx_size.setInputType(InputType.TYPE_CLASS_NUMBER);

		AppCompatEditText editTxt_temperature = new AppCompatEditText(requireContext());
		editTxt_temperature.setHint("Temperature (example: 0.8)");
		editTxt_temperature.setText(Float.toString(model_info.getTemperature()));
		editTxt_temperature.setSingleLine();
		editTxt_temperature.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

		AppCompatEditText editTxt_system_info = new AppCompatEditText(requireContext());
		editTxt_system_info.setHint("System information (remove any current date/time - that's automatic)");
		editTxt_system_info.setText(model_info.getSystem_info());

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save model");
		btn_save.setOnClickListener(v -> {
			GPTComm.addUpdateModelOLLAMA(
					model_name,
					spinner_model_type.getSelectedItemPosition() == 0 ? GPTComm.MODEL_TYPE_TEXT : GPTComm.MODEL_TYPE_VISION,
					switch_model_has_tool_role.isChecked(),
					Integer.parseInt(editTxt_ctx_size.getText().toString()),
					Float.parseFloat(editTxt_temperature.getText().toString()),
					editTxt_system_info.getText().toString()
			);
		});

		AppCompatButton btn_delete = new AppCompatButton(requireContext());
		btn_delete.setText("Delete model");
		btn_delete.setOnClickListener(v -> {
			Utils.createConfirmationDialog(requireContext(), "Are you sure you want to delete this model?",
				() -> {
					GPTComm.deleteModelOLLAMA(model_name);

					ModsFileInfo.Mod7UserInfo mod_7_user_info = SettingsSync.getMod7InfoUSERSETS();
					String new_model_priorities = "";
					for (final String model : mod_7_user_info.getModel_priorities().split("\n")) {
						if (!model.equals(model_name)) {
							new_model_priorities += model + "\n";
						}
					}
					if (!new_model_priorities.isEmpty()) {
						new_model_priorities = new_model_priorities.substring(0, new_model_priorities.length() - 1);
					}
					mod_7_user_info.setModel_priorities(new_model_priorities);

					Utils.reloadFragment(this);
			});
		});

		child_views.add(txtView_model_name);
		child_views.add(spinner_model_type);
		child_views.add(switch_model_has_tool_role);
		child_views.add(editTxt_ctx_size);
		child_views.add(editTxt_temperature);
		child_views.add(editTxt_system_info);
		child_views.add(Utils.createHorizontalButtonBar(requireContext(), btn_save, btn_delete));

		return child_views;
	}
}
