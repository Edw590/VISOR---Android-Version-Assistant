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

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class GenericExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private List<Pair<String, List<View>>> items; // List to hold pairs of titles and views

	public GenericExpandableListAdapter(Context context) {
		this.context = context;
		this.items = new ArrayList<>();
	}

	// Add a new group with a title and list of views
	public void addItem(String title, List<View> views) {
		items.add(new Pair<>(title, views));
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return items.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return items.get(groupPosition).second.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return items.get(groupPosition).first;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return items.get(groupPosition).second.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition; // Using position as ID
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition; // Using position as ID
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		AppCompatTextView textView = new AppCompatTextView(context);
		textView.setText((String) getGroup(groupPosition));
		textView.setPadding(100, 20, 20, 20);
		textView.setTextSize(18);
		textView.setTypeface(null, Typeface.BOLD);
		return textView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 10, 10, 10);

		// Get the specific view for this child position
		View view = items.get(groupPosition).second.get(childPosition);

		// Remove the view from its parent if it already has one
		if (view.getParent() != null) {
			((ViewGroup) view.getParent()).removeView(view);
		}
		layout.addView(view);

		return layout;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
