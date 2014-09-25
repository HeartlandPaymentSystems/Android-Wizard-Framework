package com.hps.wizard;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.hps.wizard.StateFragment.StateDefinition;

/**
 * <p>
 * The default {@link Activity} for a wizard. Starting it is done via {@link WizardActivity#startWizard(Activity, StateDefinition, int, boolean, boolean, int)}.
 * For example:
 * </p>
 * 
 * <pre>
 * WizardActivity.startWizard(activity, new {@link StateDefinition}(FirstState.class, argsForState), {@link WizardActivity#STEP_COUNT_UNKNOWN}, allowPreviousPlaceholder, allowNeutralPlaceholder, requestCode);
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
public class WizardActivity extends AbstractWizardActivity {
	/**
	 * Launches a new WizardActivity with the given parameters.
	 * 
	 * @param startingActivity
	 *            the calling activity. It is used for the call to {@link Activity#startActivityForResult(Intent, int)}.
	 * @param firstState
	 *            The definition of the first state in the wizard.
	 * @param stepCount
	 *            The number of steps in the wizard. Use {@link WizardActivity#STEP_COUNT_UNKNOWN} to hide the step counter.
	 * @param allowPreviousPlaceholder
	 *            Whether an empty slot should be provided for the previous button when it's not visible.
	 * @param allowNeutralPlaceholder
	 *            Whether an empty slot should be provided for the neutral button when it's not visible.
	 * @param requestCode
	 *            The request code to use for the call to {@link Activity#startActivityForResult(Intent, int)}.
	 */
	public static void startWizard(Activity startingActivity, StateDefinition firstState, int stepCount, boolean allowPreviousPlaceholder,
			boolean allowNeutralPlaceholder, int requestCode) {
		Intent wizardIntent = new Intent(startingActivity, WizardActivity.class);
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
		 * When running as a full activity we use the views in the layout to display titles and step counts.
		 */
		String titleString = getStates().peek().getTitle();

		if (titleString == null) {
			getTitleView().setVisibility(View.GONE);
		} else {
			getTitleView().setVisibility(View.VISIBLE);
			getTitleView().setText(titleString);
		}

		updateStepCount();
	}

	/**
	 * <p>
	 * Sets the visible step count, if necessary.
	 * </p>
	 * <p>
	 * Currently this is just a text field in the form of x/y where x is the current step number and y is the number of steps. Future implementations could
	 * change this to be a graphical indicator or move the position around.
	 * </p>
	 */
	private void updateStepCount() {
		if (getStepCountTextView().getVisibility() == View.VISIBLE) {
			getStepCountTextView().setText(String.valueOf(getStates().size()) + "/" + String.valueOf(getStepCount()));
		}
	}

	@Override
	protected int getContentViewId() {
		return R.layout.activity_wizard;
	}
}
