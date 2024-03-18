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

package com.edw590.visor_c_a;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>Class that stores all {@link Task}s to be executed by VISOR.</p>
 * <p>All in here is static so the runnables will remain here after added for as long as the app process lives.</p>
 * <p>This class is thread-safe.</p>
 */
public final class TasksList {

	// By default, capacity of 10.
	static final List<Task> tasks_list = new ArrayList<>(10);

	/** Runnable that does nothing at all. */
	private static final Runnable empty_runnable = new Runnable() {@Override public void run() {}};

	/** A postponed task to be executed VISOR. */
	public static final class Task {
		/** The runnable associated with this task (what to do). */
		public final Runnable runnable;
		/** A non-negative ID unique within {@link #tasks_list} associated with this task. */
		final int task_id;
		/** The milliseconds of when the task was registered on the list. */
		public final long registered_when;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param runnable {@link #runnable}
		 */
		Task(@NonNull final Runnable runnable) {
			this.runnable = runnable;

			final Random random = new Random();
			int task_id_tmp = Math.abs(random.nextInt());
			final int tasks_list_size = tasks_list.size();
			for (int i = 0; i < tasks_list_size; ++i) {
				if (tasks_list.get(i).task_id == task_id_tmp) {
					task_id_tmp = Math.abs(random.nextInt());
					i = -1;
				}
			}
			task_id = task_id_tmp;

			registered_when = System.currentTimeMillis();
		}

		/**
		 * <p>Calls {@link Runnable#run()} on {@link #runnable}.</p>
		 */
		public void run() {
			runnable.run();
		}
	}

	/**
	 * <p>Creates and adds a task to the tasks list.</p>
	 *
	 * @param runnable the runnable associated with the task
	 *
	 * @return {@link Task#task_id}
	 */
	public static synchronized int addTask(@NonNull final Runnable runnable) {
		final Task task = new Task(runnable);
		tasks_list.add(task);

		return task.task_id;
	}

	/**
	 * <p>Removes a task from the list if it exists.</p>
	 *
	 * @param task_id the {@link Task#task_id} associated with the task
	 *
	 * @return the task, or a task with an {@link #empty_runnable} in case the given ID is negative (useful to call run()
	 * on with no effect at all) or in case it doesn't exist on the list (which will never happen - why give a wrong ID)
	 */
	@NonNull
	public static synchronized Task removeTask(final int task_id) {
		if (task_id >= 0) {
			final int runnables_size = tasks_list.size();
			for (int i = 0; i < runnables_size; ++i) {
				final Task task = tasks_list.get(i);
				if (task.task_id == task_id) {
					// Don't execute and remove right here. If the task is executed here, not sure if the function remains
					// active and then keeps the entire class locked (synchronized method).
					tasks_list.remove(i);

					return task;
				}
			}
		}

		return new Task(empty_runnable);
	}
}
