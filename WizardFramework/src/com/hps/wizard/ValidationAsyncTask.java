package com.hps.wizard;

import android.os.AsyncTask;

/**
 * <p>
 * A task which allows a {@link StateFragment} to run validation in the background. For the most part it simply pushes events up to the {@link TaskCallback},
 * which must be set by {@link #setCallback(TaskCallback)} to avoid null pointer exceptions.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * 
 * <pre>
 * public class BackgroundValidation extends StateFragment {
 * 	private class BackgroundTask extends ValidationAsyncTask {
 * 		&#064;Override
 * 		protected Boolean doInBackground(Object... params) {
 * 			// params will always be null
 * 
 * 			// do anything you'd normally do in an AsyncTask, including calls to
 * 			publishProgress(&quot;progress string&quot;);
 * 
 * 			return trueIfValidated;
 * 		}
 * 	}
 * 
 * 	&#064;Override
 * 	public boolean shouldValidateInBackground() {
 * 		return true;
 * 	}
 * 
 * 	&#064;Override
 * 	public ValidationAsyncTask getValidatorTask() {
 * 		// make sure we're no longer allowed to go forward
 * 		letUsGoForward = false;
 * 
 * 		// create the task to be run
 * 		int start = durationSlider.getProgress();
 * 		boolean shouldPass = passCheckBox.isChecked();
 * 		ValidationAsyncTask validator = new BackgroundTask(start, shouldPass);
 * 
 * 		return validator;
 * 	}
 * }
 * </pre>
 */
public abstract class ValidationAsyncTask extends AsyncTask<Object, String, Boolean> {

	/**
	 * The callback to push events to. This must be set via {@link #setCallback(TaskCallback)} to avoid null pointer exceptions.
	 */
	private TaskCallback callback;

	/**
	 * Set the callback which will receive task events.
	 * 
	 * @param callback
	 *            The callback. Cannot be null.
	 */
	public void setCallback(TaskCallback callback) {
		this.callback = callback;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		callback.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(String... values) {
		callback.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled() {
		callback.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		callback.onPreExecute();
	}

}
