package com.hps.wizard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * This Fragment manages a single background task and retains itself across configuration changes.
 */
public class TaskFragment extends Fragment {

	private TaskCallback callback;
	private ValidationAsyncTask task;

	/**
	 * Hold a reference to the parent Activity so we can report the task's current progress and results. The Android framework will pass us a reference to the
	 * newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callback = (TaskCallback) activity;
	}

	/**
	 * This method will only be called once when the retained Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);
	}

	/**
	 * Executes the given {@link ValidationAsyncTask}.
	 * 
	 * @param validationTask
	 *            The task to execute.
	 */
	void execute(ValidationAsyncTask validationTask) {
		this.task = validationTask;
		task.setCallback(callback);
		Object noParameters = null;
		task.execute(noParameters);
	}

	/**
	 * Set the callback to null so we don't accidentally leak the Activity instance.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		callback = null;
	}
}