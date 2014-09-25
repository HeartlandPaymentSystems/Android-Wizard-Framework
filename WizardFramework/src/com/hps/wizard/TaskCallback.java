package com.hps.wizard;

import android.os.AsyncTask;

/**
 * Callback interface through which the {@link TaskFragment} will report the task's progress and results back to the {@link AbstractWizardActivity}.
 */
public interface TaskCallback {
	/**
	 * Callback method for use in {@link AsyncTask#onPreExecute}. It runs before the task is executed and updates the UI to prepare for the background
	 * activities.
	 */
	void onPreExecute();

	/**
	 * Callback method for use in {@link AsyncTask#onProgressUpdate}. It runs when the background task calls publishProgress(String) and updates the UI to
	 * reflect the new progress.
	 */
	void onProgressUpdate(String... progress);

	/**
	 * Callback method for use in {@link AsyncTask#onCancelled}. It runs when the background task is cancelled.
	 */
	void onCancelled();

	/**
	 * Callback method for use in {@link AsyncTask#onPostExecute}. It runs after execution and reports whether the validation passed or failed.
	 * 
	 * @param result
	 */
	void onPostExecute(Boolean result);
}