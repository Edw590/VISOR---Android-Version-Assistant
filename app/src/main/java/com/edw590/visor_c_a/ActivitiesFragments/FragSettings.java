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

package com.edw590.visor_c_a.ActivitiesFragments;

import androidx.fragment.app.Fragment;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class FragSettings extends Fragment {

	/*static final Value[] SETTINGS_ARRAY_CLONE = SettingsRegistry.getArray();
	static final int SETTINGS_ARRAY_LENGTH = SettingsRegistry.getArray().length;

	View frag_view;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_settings, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		frag_view = view;

		final Button button_save_settings = view.findViewById(R.id.button_save_settings);
		button_save_settings.setOnClickListener(onClickListener);

		final LinearLayout linearLayout = view.findViewById(R.id.frag_settings_linear_layout);
		final LayoutInflater layoutInflater = LayoutInflater.from(requireContext());

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 SP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.0F,
				resources.getDisplayMetrics());

		for (int i = 0; i < SETTINGS_ARRAY_LENGTH; ++i) {
			final Value value = SETTINGS_ARRAY_CLONE[i]; // Add a TextView for each value.

			final TextView textView = new TextView(requireContext());
			textView.setLayoutParams(layoutParams);
			textView.setPadding(padding_px, padding_px, padding_px, 0);
			textView.setTextColor(Color.BLACK);
			textView.setTextIsSelectable(true);
			final String text = "Name: " + value.pretty_name + "\nType: " + value.type;
			textView.setText(text);
			linearLayout.addView(textView);

			final ExpandableTextView expandableTextView = (ExpandableTextView) layoutInflater
					.inflate(R.layout.expandable_text_view, null);
			expandableTextView.setText(value.description);
			expandableTextView.setPadding(padding_px, 0, padding_px, padding_px);
			linearLayout.addView(expandableTextView);

			if (value.type.equals(Value.TYPE_BOOLEAN)) {
				final SwitchCompat switchCompat = new SwitchCompat(requireContext());
				switchCompat.setChecked(value.getData());
				switchCompat.setId(i);
				linearLayout.addView(switchCompat);
			} else {
				final EditText editText = new EditText(requireContext());
				editText.setId(i);
				switch (value.type) {
					case (Value.TYPE_INTEGER):
					case (Value.TYPE_LONG): {
						editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

						break;
					}
					case (Value.TYPE_DOUBLE): {
						editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
								InputType.TYPE_NUMBER_FLAG_DECIMAL);

						break;
					}
				}
				linearLayout.addView(editText);
			}
		}
	}

	private final View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			for (int i = 0; i < SETTINGS_ARRAY_LENGTH; ++i) {
				final Value value = SettingsRegistry.getValueObj(i);
				if (value.type.equals(Value.TYPE_BOOLEAN)) {
					final SwitchCompat switchCompat = frag_view.findViewById(i);
					UtilsRegistry.setValue(UtilsRegistry.LIST_SETTINGS, value.key, switchCompat.isChecked());
				} else {
					final EditText editText = frag_view.findViewById(i);
					UtilsRegistry.setValue(UtilsRegistry.LIST_SETTINGS, value.key, editText.getText());
				}
			}
		}
	};*/
}
