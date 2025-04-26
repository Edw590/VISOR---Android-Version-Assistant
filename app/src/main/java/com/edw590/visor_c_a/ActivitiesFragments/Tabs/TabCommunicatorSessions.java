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

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsTimeDate;
import com.edw590.visor_c_a.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import GPTComm.GPTComm;
import UtilsSWA.UtilsSWA;


/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class TabCommunicatorSessions extends Fragment {

	final class SessionInfo {
		String id;
		ModsFileInfo.Session session;
	}

	HashMap<String, AppCompatTextView> txtViews_map = new LinkedHashMap<>(50);

	private int padding_px = 0;

	@Override
	public void onStart() {
		super.onStart();

		try {
			infinity_checker.start();
		} catch (final IllegalThreadStateException ignored) {
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		infinity_checker.interrupt();
	}

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

		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 DP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0F,
				resources.getDisplayMetrics());

		if (!UtilsSWA.isCommunicatorConnectedSERVER()) {
			AppCompatTextView txtView = new AppCompatTextView(requireContext());
			txtView.setText("[Not connected to the server to get the chats]");

			linearLayout.addView(txtView);

			return;
		}

		String sessions_ids_str = GPTComm.getSessionIdsList();
		if (sessions_ids_str.isEmpty()) {
			AppCompatTextView txtView = new AppCompatTextView(requireContext());
			txtView.setText("[Not connected to the server to get the chats]");

			linearLayout.addView(txtView);

			return;
		}

		List<SessionInfo> sessions_info = new ArrayList<>(50);
		String[] sessions_ids = sessions_ids_str.split("\\|");
		for (final String session_id : sessions_ids) {
			SessionInfo session_info = new SessionInfo();
			session_info.id = session_id;
			session_info.session = new ModsFileInfo.Session();
			session_info.session.setName(GPTComm.getSessionName(session_id));
			session_info.session.setCreated_time_s(GPTComm.getSessionCreatedTime(session_id));
			sessions_info.add(session_info);
		}

		Collections.sort(sessions_info, (s1, s2) -> {
			if (s1.session.getCreated_time_s() == s2.session.getCreated_time_s()) {
				return 0;
			}

			return s1.session.getCreated_time_s() > s2.session.getCreated_time_s() ? -1 : 1;
		});

		ExpandableListView expandable_list_view = new ExpandableListView(requireContext());
		GenericExpandableListAdapter adapter = new GenericExpandableListAdapter(requireContext());
		expandable_list_view.setAdapter(adapter);
		expandable_list_view.setLayoutParams(linearLayout.getLayoutParams());
		expandable_list_view.setOnGroupCollapseListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view, true);
		});
		expandable_list_view.setOnGroupExpandListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view, true);
		});

		linearLayout.addView(expandable_list_view);

		for (final SessionInfo session_info : sessions_info) {
			if (session_info.id.equals("temp") || session_info.id.equals("dumb")) {
				continue;
			}

			String title = session_info.session.getName() + " - " +
					UtilsTimeDate.getTimeDateStr(GPTComm.getSessionCreatedTime(session_info.id));

			adapter.addItem(title, createSessionView(session_info));
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view, true);
	}

	private List<View> createSessionView(final SessionInfo session_info) {
		List<View> child_views = new ArrayList<>(10);

		AppCompatEditText editTxt_name = new AppCompatEditText(requireContext());
		editTxt_name.setText(session_info.session.getName());
		editTxt_name.setHint("Chat name");
		editTxt_name.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save name");
		btn_save.setOnClickListener(v -> {
			if (editTxt_name.getText().toString().isEmpty()) {
				Utils.createErrorDialog(requireContext(), "The chat name must not be empty");

				return;
			}

			GPTComm.setSessionName(session_info.id, editTxt_name.getText().toString());

			Utils.reloadFragment(this);
		});

		AppCompatButton btn_delete = new AppCompatButton(requireContext());
		btn_delete.setText("Delete chat");
		btn_delete.setOnClickListener(v -> {
			Utils.createConfirmationDialog(requireContext(), "Are you sure you want to delete this chat?",
					() -> {
						GPTComm.deleteSession(session_info.id);

						Utils.reloadFragment(this);
					});
		});

		AppCompatTextView txtView_history = new AppCompatTextView(requireContext());
		txtView_history.setPadding(padding_px, padding_px, padding_px, padding_px);
		txtView_history.setTextIsSelectable(true);
		txtViews_map.put(session_info.id, txtView_history);

		child_views.add(editTxt_name);
		child_views.add(Utils.createHorizontalButtonBar(requireContext(), btn_save, btn_delete));
		child_views.add(txtView_history);

		return child_views;
	}

	private final class Runnable1 implements Runnable {

		final String session_id;
		final String msg_content;

		Runnable1(final String session_id, final String msg_content) {
			this.session_id = session_id;
			this.msg_content = msg_content;
		}

		@Override
		public void run() {
			AppCompatTextView txtView = txtViews_map.get(session_id);
			if (txtView != null) {
				txtView.setText(msg_content);
			}
		}
	}

	private final Thread infinity_checker = new Thread(() -> {
		while (true) {
			if (UtilsSWA.isCommunicatorConnectedSERVER()) {
				String sessions_ids_str = GPTComm.getSessionIdsList();
				if (!sessions_ids_str.isEmpty()) {
					String[] sessions_ids = sessions_ids_str.split("\\|");
					for (final String session_id : sessions_ids) {
						if (session_id.equals("temp") || session_id.equals("dumb")) {
							continue;
						}

						String[] session_history = GPTComm.getSessionHistory(session_id).split("\0");
						String msg_content_str = "";
						for (final String message : session_history) {
							String[] message_parts_pipe = message.split("\\|");
							String[] message_parts_slash = message_parts_pipe[0].split("/");

							String msg_role = message_parts_slash[0];
							switch (msg_role) {
								case "assistant": {
									msg_role = "VISOR";

									break;
								}
								case "user": {
									msg_role = "YOU";

									break;
								}
								default: {
									continue;
								}
							}

							if (message_parts_pipe.length < 2 || message_parts_pipe[1].isEmpty()) {
								// Means no message (so maybe was a "SYSTEM TASK" message - ignore those)
								continue;
							}

							long msg_timestamp_s = Long.parseLong(message_parts_slash[1]);
							String msg_content = message_parts_pipe[1];

							msg_content_str +=
									"-----------------------------------------------------------------------\n" +
											"|" + msg_role + "| on " +
											UtilsTimeDate.getTimeDateStr(msg_timestamp_s) + ":\n" +
											msg_content + "\n\n";
						}
						if (msg_content_str.length() > 2) {
							msg_content_str = msg_content_str.substring(0, msg_content_str.length() - 2);
						}

						AppCompatTextView txtView = txtViews_map.get(session_id);
						if (txtView != null && !msg_content_str.equals(txtView.toString())) {
							requireActivity().runOnUiThread(new Runnable1(session_id, msg_content_str));
						}
					}
				}
			}

			try {
				Thread.sleep(5000);
			} catch (final InterruptedException ignored) {
				return;
			}
		}
	});
}
