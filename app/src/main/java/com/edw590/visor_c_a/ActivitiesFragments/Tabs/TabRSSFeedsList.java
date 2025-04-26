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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import java.util.ArrayList;
import java.util.List;

import SettingsSync.SettingsSync;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class TabRSSFeedsList extends Fragment {

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

		linearLayout.addView(expandable_list_view);

		String feeds_ids_str = SettingsSync.getIdsListRSS();
		if (!feeds_ids_str.isEmpty()) {
			String[] feed_ids = feeds_ids_str.split("\\|");
			for (final String feed_id : feed_ids) {
				ModsFileInfo.FeedInfo feed_info = SettingsSync.getFeedRSS(Integer.parseInt(feed_id));
				String title = "";
				if (!feed_info.getEnabled()) {
					title += "[X] ";
				}
				title += feed_info.getName();

				adapter.addItem(title, createFeedInfoSetter(feed_info));
			}
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view, false);
	}

	private List<View> createFeedInfoSetter(final ModsFileInfo.FeedInfo feed_info) {
		List<View> child_views = new ArrayList<>(10);

		AppCompatCheckBox check_enabled = new AppCompatCheckBox(requireContext());
		check_enabled.setText("Feed enabled");
		check_enabled.setChecked(feed_info.getEnabled());

		AppCompatEditText editTxt_name = new AppCompatEditText(requireContext());
		editTxt_name.setText(feed_info.getName());
		editTxt_name.setHint("Feed name");
		editTxt_name.setSingleLine();

		AppCompatEditText editTxt_type = new AppCompatEditText(requireContext());
		editTxt_type.setText(feed_info.getType_());
		editTxt_type.setHint("Feed type (\"General\" or \"YouTube [CH|PL] [+S]\")");
		editTxt_type.setSingleLine();

		AppCompatEditText editTxt_url = new AppCompatEditText(requireContext());
		editTxt_url.setText(feed_info.getUrl());
		editTxt_url.setHint("Feed URL or YouTube playlist/channel ID");
		editTxt_url.setSingleLine();

		AppCompatEditText editTxt_custom_msg_subject = new AppCompatEditText(requireContext());
		editTxt_custom_msg_subject.setText(feed_info.getCustom_msg_subject());
		editTxt_custom_msg_subject.setHint("Custom message subject (for YT it's automatic)");
		editTxt_custom_msg_subject.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			feed_info.setEnabled(check_enabled.isChecked());
			feed_info.setName(editTxt_name.getText().toString());
			feed_info.setType_(editTxt_type.getText().toString());
			feed_info.setUrl(editTxt_url.getText().toString());
			feed_info.setCustom_msg_subject(editTxt_custom_msg_subject.getText().toString());

			Utils.reloadFragment(this);
		});

		AppCompatButton btn_delete = new AppCompatButton(requireContext());
		btn_delete.setText("Delete");
		btn_delete.setOnClickListener(v -> {
			Utils.createConfirmationDialog(requireContext(), "Are you sure you want to delete this feed?",
					() -> {
						SettingsSync.removeFeedRSS(feed_info.getId());

						Utils.reloadFragment(this);
					});
		});

		child_views.add(check_enabled);
		child_views.add(editTxt_name);
		child_views.add(editTxt_type);
		child_views.add(editTxt_url);
		child_views.add(editTxt_custom_msg_subject);
		child_views.add(Utils.createHorizontalButtonBar(requireContext(), btn_save, btn_delete));

		return child_views;
	}
}
