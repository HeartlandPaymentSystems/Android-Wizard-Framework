package com.hps.wizard;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.hps.wizard.StateFragment.StateDefinition;

/**
 * <p>
 * The default {@link Activity} for a wizard. Starting it is done via {@link WizardDialog#startWizard(Activity, StateDefinition, int, boolean, boolean, int)}.
 * For example:
 * </p>
 * 
 * <pre>
 * WizardActivity.startWizard(activity, new {@link StateDefinition}(FirstState.class, argsForState), {@link WizardDialog#STEP_COUNT_UNKNOWN}, allowPreviousPlaceholder, allowNeutralPlaceholder, requestCode);
 * </pre>
 * 
 * <p>
 * When the wizard is complete it will set its result to {@link Activity#RESULT_CANCELED} if the user backed out or {@link Activity#RESULT_OK} if it reached its
 * conclusion.
 * </p>
 * 
 * <h2>Important</h2>
 * <p>
 * See AndroidManifest.xml for activity declarations that must be made in the host library's manifest.
 * </p>
 */
public class WizardDialog extends AbstractWizardActivity {
	private static final String TAG = "WizardDialog";

	/**
	 * Launches a new WizardActivity with the given parameters.
	 * 
	 * @param startingActivity
	 *            the calling activity. It is used for the call to {@link Activity#startActivityForResult(Intent, int)}.
	 * @param firstState
	 *            The definition of the first state in the wizard.
	 * @param stepCount
	 *            The number of steps in the wizard. Use {@link WizardDialog#STEP_COUNT_UNKNOWN} to hide the step counter.
	 * @param allowPreviousPlaceholder
	 *            Whether an empty slot should be provided for the previous button when it's not visible.
	 * @param allowNeutralPlaceholder
	 *            Whether an empty slot should be provided for the neutral button when it's not visible.
	 * @param requestCode
	 *            The request code to use for the call to {@link Activity#startActivityForResult(Intent, int)}.
	 */
	public static void startWizard(Activity startingActivity, StateDefinition firstState, int stepCount, boolean allowPreviousPlaceholder,
			boolean allowNeutralPlaceholder, int requestCode) {
		Intent wizardIntent = new Intent(startingActivity, WizardDialog.class);
		wizardIntent.putExtra(FIRST_STATE_CLASS, firstState.clazz);
		wizardIntent.putExtra(STEP_COUNT, stepCount);
		wizardIntent.putExtra(ALLOW_NEUTRAL_BUTTON_PLACEHOLDER, allowNeutralPlaceholder);
		wizardIntent.putExtra(ALLOW_PREVIOUS_BUTTON_PLACEHOLDER, allowPreviousPlaceholder);
		wizardIntent.putExtras(firstState.args);

		startingActivity.startActivityForResult(wizardIntent, requestCode);
	}

	@Override
	protected void initTitle() {
		/**
		 * The dialog layout hides the title and step count views so we can show them as the actual dialog title.
		 */
		String titleString = getStates().peek().getTitle();
		if (getStepCount() != STEP_COUNT_UNKNOWN) {
			String stepCountString = String.valueOf(getStates().size()) + "/" + String.valueOf(getStepCount());
			StringBuilder titleBuilder = new StringBuilder(stepCountString);
			titleBuilder.append(" ");
			titleBuilder.append(titleString);
			titleString = titleBuilder.toString();
		}

		setTitle(titleString);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * Make us non-modal, so that others can receive touch events, but notify us that it happened. In onTouchEvent we'll swallow any events that happen
		 * outside of our window.
		 */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		super.onCreate(savedInstanceState);

		initializeAsDialog();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/**
		 * If we've received a touch notification that the user has touched outside the window, swallow it.
		 */
		if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
			return true;
		}

		/**
		 * Delegate everything else to Activity.
		 */
		return super.onTouchEvent(event);
	}

	/**
	 * Sets the proper dialog size based on the screen's height and width.
	 */
	private void initializeAsDialog() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		Log.i(TAG, String.format("Screen dimensions: height %d, width %d", metrics.heightPixels, metrics.widthPixels));

		int dialogWidth = getWidthToDisplay(metrics);
		int dialogHeight = getHeightToDisplay(metrics);

		Log.i(TAG, String.format("Dialog dimensions: height %d, width %d", dialogHeight, dialogWidth));

		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.x = -20;
		params.y = -10;
		params.height = dialogHeight;
		params.width = dialogWidth;

		getWindow().setAttributes(params);
	}

	/**
	 * Gets the suggested height of the dialog, which is 90% of the height of the screen.
	 * 
	 * @param metrics
	 *            An instance of {@link DisplayMetrics} that holds the dimensions of the screen.
	 * @return the suggested height for this dialog.
	 */
	protected int getHeightToDisplay(DisplayMetrics metrics) {
		int dialogHeight = metrics.heightPixels * 9 / 10; // set height to 90% of screen
		return dialogHeight;
	}

	/**
	 * <p>
	 * Gets the suggested width of the dialog, which depends upon the screen size and orientation. This will be 90% of the width of the scren unless we are
	 * running in landscape mode on a screen that reports as large (i.e. R.bool.small_screen is false). Int that case the suggested width will be 55% of the
	 * base screen width.
	 * </p>
	 * 
	 * @param metrics
	 *            An instance of {@link DisplayMetrics} that holds the dimensions of the screen.
	 * @return the suggested height for this dialog.
	 */
	protected int getWidthToDisplay(DisplayMetrics metrics) {
		boolean isSmallScreen = getResources().getBoolean(R.bool.small_screen);
		if (isSmallScreen) {
			return metrics.widthPixels * 9 / 10;
		} else {
			// on larger screens we need to constrain the width to a max of 700dpi
			int dialogWidth = metrics.widthPixels * 9 / 10; // set width to 90% of screen

			// If we're on a large landscape screen, limit dialogs to 55% the width of the screen
			int maxLandscapeWidth = (int) (metrics.widthPixels * 0.55);
			if (isLandscape() && dialogWidth > maxLandscapeWidth) {
				dialogWidth = maxLandscapeWidth;
			}
			return dialogWidth;
		}
	}

	/**
	 * @return true if the screen is running in landscape mode, false otherwise.
	 */
	private boolean isLandscape() {
		int orientation = getResources().getConfiguration().orientation;
		return (orientation == Configuration.ORIENTATION_LANDSCAPE);
	}

	@Override
	protected int getContentViewId() {
		return R.layout.dialog_wizard;
	}

}
