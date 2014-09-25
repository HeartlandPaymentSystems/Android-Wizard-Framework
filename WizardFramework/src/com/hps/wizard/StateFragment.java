package com.hps.wizard;

import java.io.Serializable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * This class acts as the parent class for all states.
 */
public abstract class StateFragment extends Fragment {

	private AbstractWizardActivity wizardActivity;

	/**
	 * The definition of a state, including its class and arguments.
	 */
	public static final class StateDefinition implements Serializable {
		private static final long serialVersionUID = -696843153473982446L;
		public final Bundle args;
		public final Class<? extends StateFragment> clazz;

		/**
		 * @param stateClass
		 *            The class, which must extend {@link StateFragment}.
		 * @param arguments
		 *            The arguments to be passed to the class when it is instantiated.
		 */
		public StateDefinition(Class<? extends StateFragment> stateClass, Bundle arguments) {
			if (!StateFragment.class.isAssignableFrom(stateClass)) {
				throw new IllegalArgumentException("A state definition's class must extend StateFragment. Given class " + stateClass.getSimpleName()
						+ " does not.");
			}
			clazz = stateClass;
			args = arguments;
		}
	}

	/**
	 * Create a new instance of a Fragment with the given class name. This method should be used in place of the constructors as it ensures the
	 * {@link AbstractWizardActivity} gets set.
	 * 
	 * @param wizardActivity
	 *            The activity for this state.
	 * @param name
	 *            The class name of the fragment to instantiate.
	 * @param extras
	 *            Bundle of arguments to supply to the fragment, which it can retrieve with getArguments(). May be null.
	 * @return a new StateFragment instance.
	 */
	static StateFragment create(AbstractWizardActivity wizardActivity, String name, Bundle extras) {
		StateFragment state = (StateFragment) instantiate(wizardActivity, name, extras);
		state.wizardActivity = wizardActivity;
		return state;
	}

	/**
	 * <p>
	 * Called to make sure a next click should be honored. Any form validation should go here.
	 * </p>
	 * <p>
	 * The default implementation simply returns true.
	 * </p>
	 * 
	 * @return true if this state can move forward, false otherwise.
	 */
	protected boolean canGoForward() {
		return true;
	}

	/**
	 * Get the bundle to use in {{@link #getSavedInstanceState()}. This is not necessary, but it will ensure that the bundle contains all of the arguments which
	 * were originally given to the fragment at instantiation.
	 * 
	 * @return The fragment's arguments or an empty bundle if they were null.
	 */
	protected Bundle getBundleToSave() {
		Bundle bundle = getArguments();
		if (bundle == null) {
			bundle = new Bundle();
		}
		return bundle;
	}

	/**
	 * If this is the final state, this method will be called to retrieve the intent that should be passed to {@link Activity#setResult(int, Intent)}. It can be
	 * null if there is no need to tell the original activity anything about this wizard.
	 * 
	 * @return The intent, or null if none is needed.
	 */
	public Intent getFinalResult() {
		return null;
	}

	/**
	 * <p>
	 * Gets the label for the button. Override this method to make it appear.
	 * </p>
	 * 
	 * @return The label to put on the neutral button. Returning null hides the button.
	 */
	public String getNeutralButtonLabel() {
		return null;
	}

	/**
	 * <p>
	 * Gets the label for the button.
	 * </p>
	 * <p>
	 * The default implementation returns R.string.wizard_next or R.string.wizard_finish if {@link #isFinal()} returns true.
	 * </p>
	 * 
	 * @return The label to put on the next button. Returning null hides the button.
	 */
	public String getNextButtonLabel() {
		if (isFinal()) {
			return wizardActivity.getString(R.string.wizard_finish);
		} else {
			return wizardActivity.getString(R.string.wizard_next);
		}
	}

	/**
	 * Assuming there is a next state, this method is called to return the class of that state. It must not be null and must extend {@link StateFragment}. If
	 * there is no next state, canGoForward should return false.
	 * 
	 * @return the {@link StateDefinition} for the next state.
	 */
	abstract public StateDefinition getNextState();

	/**
	 * <p>
	 * Gets the label for the button. Override this method to make it appear.
	 * </p>
	 * 
	 * @return The label to put on the previous button. Returning null hides the button.
	 */
	public String getPreviousButtonLabel() {
		return null;
	}

	/**
	 * <p>
	 * Called when the {@link StateFragment} should save it's current state. The bundle returned will be passed to
	 * {@link Fragment#instantiate(android.content.Context, String, Bundle)} if this StateFragment is recreated.
	 * </p>
	 * 
	 * <p>
	 * The default implementation simply returns null. Override this if there is any information that should survive the wizard activity being destroyed and
	 * recreated.
	 * </p>
	 * 
	 * @return the bundle of data, which can be null.
	 */
	public Bundle getSavedInstanceState() {
		return getBundleToSave();
	}

	/**
	 * <p>
	 * Gets the title for this view. Override this method to place a title on a step.
	 * </p>
	 * 
	 * <p>
	 * Giving a null title will hide the title view. Giving empty titles in some steps and non-empty ones in others could result in the content area seeming to
	 * bounce around and thus should be avoided.
	 * </p>
	 * 
	 * @return the title, or null to hide the space which is otherwise reserved for it.
	 */
	public String getTitle() {
		return null;
	}

	/**
	 * @return the {@link AbstractWizardActivity} that was set in {@link #create(AbstractWizardActivity, String, Bundle)}.
	 */
	protected AbstractWizardActivity getWizardActivity() {
		return wizardActivity;
	}

	/**
	 * <p>
	 * Returns true if this is the last state.
	 * </p>
	 * <p>
	 * The default implementation returns false. Override this if the state is the last one in the wizard. If it's the last state it's Next button will be
	 * replaced by a finish button. The visibility of the previous button still depends on {@link #getPreviousButtonLabel()}.
	 * </p>
	 * 
	 * @return true if this is the last state, false otherwise.
	 */
	protected boolean isFinal() {
		return false;
	}

	/**
	 * <p>
	 * Called when the state has been added to the wizard. At this point the activity and wizard will both be available and any view setup can be performed.
	 * </p>
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	public void onAdded() {
		// do nothing
	}

	/**
	 * <p>
	 * Called when the state is moving back to the previous state. This is a good place to save off anything that needs to survive the lifetime of the wizard.
	 * </p>
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	public void onBack() {
		// do nothing
	}

	/**
	 * <p>
	 * Called when the state is moving forward to the next state. This is a good place to save off anything that needs to survive the lifetime of the wizard.
	 * </p>
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	public void onForward() {
		// do nothing
	}

	/**
	 * <p>
	 * Called when the neutral button is clicked.
	 * </p>
	 * <p>
	 * The default implementation throws an exception. If you're going to add a neutral button you've got to override this and handle its clicks.
	 * </p>
	 */
	public void onNeutralButtonClicked() {
		throw new IllegalStateException(
				"neutral button clicked without an overridden instance of onNeutralButtonclicked. A step with a neutral button must define its own handler for clicks.");
	}

	/**
	 * <p>
	 * Perform any necessary validation prior to moving on.
	 * </p>
	 * <p>
	 * The default implementation always returns true.
	 * </p>
	 * 
	 * @return true if the state is valid and we can keep going, false otherwise.
	 */
	public boolean validate() {
		return true;
	}

	/**
	 * <p>
	 * Determines whether to perform validation on the main thread or on a background thread. If the validation is done on a background thread,
	 * {@link #validate()} will not be called. Instead, {@link #getValidatorTask()} will be used to retrieve the {@link ValidationAsyncTask} for the
	 * {@link AbstractWizardActivity} to run.
	 * </p>
	 * 
	 * <p>
	 * The framework handles all background processing and will display an indeterminate progress bar which can be updated. See {@link ValidationAsyncTask} and
	 * {@link TaskFragment} for more.
	 * </p>
	 * <p>
	 * <b>Note:</b> If validation includes network activity newer versions of Android will throw an exception if that activity occurs on the UI thread.
	 * Therefore, any validation against a server should always be done on the background.
	 * </p>
	 * <p>
	 * The default implementation always returns false.
	 * </p>
	 * 
	 * @return true if validation should be performed on the background thread, false if it belongs on the UI thread.
	 */
	public boolean shouldValidateInBackground() {
		return false;
	}

	/**
	 * <p>
	 * Get the {@link ValidationAsyncTask} to use when validating this state in the background.
	 * </p>
	 * <p>
	 * The default implementation uses foreground validation and therefore returns null.
	 * </p>
	 * 
	 * @return the task, or null if this state is validated in the foreground.
	 */
	public ValidationAsyncTask getValidatorTask() {
		return null;
	}

	/**
	 * <p>
	 * Called to make sure a back click should be honored.
	 * </p>
	 * <p>
	 * The default implementation simply returns true.
	 * </p>
	 * 
	 * @return true if this state can move forward, false otherwise.
	 */
	protected boolean canGoBack() {
		return true;
	}

	/**
	 * <p>
	 * Called when the current state is the first state and the back button should act like the system back button, finishing this wizard.
	 * </p>
	 * <p>
	 * The default implementation uses foreground validation and therefore returns null.
	 * </p>
	 * 
	 * @return
	 */
	protected boolean backShouldFinish() {
		return false;
	}

}
