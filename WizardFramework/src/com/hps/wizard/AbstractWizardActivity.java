package com.hps.wizard;

import java.util.Stack;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hps.wizard.StateFragment.StateDefinition;

/**
 * <p>
 * The default {@link Activity} for a wizard. Starting it is done via {@link Activity#startActivity(android.content.Intent, Bundle)}. For example:
 * </p>
 * 
 * <pre>
 * Intent wizardIntent = new Intent(this, WizardActivity.class);
 * wizardIntent.putExtra(WizardActivity.FIRST_STATE_CLASS, MyFirstState.class);
 * startActivityForResult(wizardIntent, 0);
 * </pre>
 * 
 * <p>
 * When the wizard is complete it will set its result to {@link Activity#RESULT_CANCELED} if the user bcaked out or {@link Activity#RESULT_OK} if it reached its
 * conclusion.
 * </p>
 */
public abstract class AbstractWizardActivity extends FragmentActivity implements TaskCallback {
	private static final String TAG = "WizardActivity";

	protected static final String ALLOW_NEUTRAL_BUTTON_PLACEHOLDER = "allowNeutralPlaceholder";
	protected static final String ALLOW_PREVIOUS_BUTTON_PLACEHOLDER = "allowPreviousPlaceholder";
	protected static final String FIRST_STATE_CLASS = "firstState";
	protected static final String SAVED_CLASS_STACK = "ClassStack";
	protected static final String SAVED_DATA_STACK = "StateData";
	protected static final String STEP_COUNT = "stepCount";
	protected static final String TASK_FRAGMENT_TAG = "taskFragment";

	/** Button ID for the Neutral button. */
	public static final int NEUTRAL_BUTTON = 3;
	/** Used in {@link #startWizard} when the step count is not known. */
	public static final int STEP_COUNT_UNKNOWN = -1;
	/** Button ID for the Next button. */
	public static final int NEXT_BUTTON = 1;
	/** Button ID for the Previous button. */
	public static final int PREVIOUS_BUTTON = 2;

	/** The fragment to use when a state requires background validation. This ensures that the validation continues uninterrupted across orientation changes. */
	private TaskFragment taskFragment;

	/** The dialog to use for showing progress during background validation. */
	private static ProgressDialog progressDialog;

	private boolean allowNeutralPlaceholder, allowPreviousPlaceholder;
	private Button previous, neutral, next;
	private Stack<StateFragment> states = new Stack<StateFragment>();
	private int stepCount;
	private TextView title, stepCountTextView;

	/**
	 * Adds the state to the stack.
	 * 
	 * @param state
	 *            the state to add (cannot be null)
	 */
	private void addState(StateFragment state) {
		if (state == null) {
			throw new IllegalArgumentException("null state cannot be added!");
		}
		getStates().push(state);
	}

	/**
	 * Builds the state stack from the given classes and bundle.
	 * 
	 * @param classStack
	 * @param dataStack
	 */
	private void buildStates(Stack<Class<? extends StateFragment>> classStack, Stack<Bundle> dataStack) {

		int i = 0;
		for (Class<? extends StateFragment> clazz : classStack) {
			Bundle extras = dataStack.get(i++);
			StateFragment state;

			Fragment foundFragment = getSupportFragmentManager().findFragmentByTag(clazz.getSimpleName());

			if (foundFragment != null) {
				state = (StateFragment) foundFragment;
			} else {
				state = StateFragment.create(this, clazz.getName(), extras);
			}

			addState(state);

			Log.i(TAG, "state " + clazz.getSimpleName() + " (" + state + ") to the stack with extras " + extras);
		}

		showState(getStates().peek());
	}

	/**
	 * Enable or disable the given button. This call is ignored if the requester is not the current state.
	 */
	public void enableButton(int buttonId, boolean enabled, StateFragment requester) {
		if (requester != getStates().peek()) {
			// ignore requests from any state but the current one
			Log.w(TAG, "change requested by " + requester.getClass().getName() + " but current state is " + getStates().peek().getClass().getName(),
					new IllegalStateException());
			return;
		}

		switch (buttonId) {
		case NEUTRAL_BUTTON:
			neutral.setEnabled(enabled);
			break;
		case NEXT_BUTTON:
			next.setEnabled(enabled);
			break;
		case PREVIOUS_BUTTON:
			previous.setEnabled(enabled);
			break;
		default:
			throw new IllegalArgumentException("Unrecognized button id " + buttonId);
		}
	}

	/**
	 * Initialize the simple fields from the given extras.
	 * 
	 * @param extras
	 */
	private void getBasicDataFromBundle(Bundle extras) {
		allowNeutralPlaceholder = extras.getBoolean(ALLOW_NEUTRAL_BUTTON_PLACEHOLDER);
		allowPreviousPlaceholder = extras.getBoolean(ALLOW_PREVIOUS_BUTTON_PLACEHOLDER);

		stepCount = extras.getInt(STEP_COUNT, STEP_COUNT_UNKNOWN);

		if (getStepCount() == STEP_COUNT_UNKNOWN) {
			stepCountTextView.setVisibility(View.GONE);
		} else {
			stepCountTextView.setVisibility(View.VISIBLE);
		}
	}

	private void handleBackClicked() {
		StateFragment state = getStates().peek();
		if (state.canGoBack()) {
			goBackAStep();
		}
	}

	/**
	 * Move back one step in the wizard.
	 */
	protected void goBackAStep() {
		if (getStates().size() < 2) {
			if (getStates().peek().backShouldFinish()) {
				onBackPressed();
			} else {
				Toast.makeText(this, "There ain't no goin' back!", Toast.LENGTH_SHORT).show();
				Log.i(TAG, "no prior state found");
			}
		} else {
			getStates().peek().onBack();

			getStates().pop();
			StateFragment lastState = getStates().peek();
			Log.i(TAG, "going back to " + lastState.getClass().getSimpleName());

			showState(lastState);
		}
	}

	/**
	 * Set the result to RESULT_OK and finish this activity so the caller can do his thing.
	 */
	private void handleFinishClicked() {
		setResult(RESULT_OK, getStates().peek().getFinalResult());
		finish();
	}

	/**
	 * The next button was clicked. Here we ask the current state if we are allowed to go forward. If we are, ask it for the next state and go.
	 */
	private void handleNextClicked() {
		StateFragment state = getStates().peek();
		if (state.canGoForward()) {
			if (state.shouldValidateInBackground()) {
				/**
				 * This task is validating in the background. Grab its ValidationTask and let the taskFragment work its magic.
				 */
				ValidationAsyncTask task = state.getValidatorTask();
				taskFragment.execute(task);

				return;
			} else if (state.validate()) { // Run validation in the foreground and respond to the results immediately.
				handleSuccessfulValidation();
			}
		}
	}

	/**
	 * Move to the next state.
	 */
	private void handleSuccessfulValidation() {
		StateFragment state = getStates().peek();
		state.onForward();
		StateDefinition def = state.getNextState();
		moveForwardToState(def.clazz, def.args);
	}

	/**
	 * Initialize the buttons based on the state's requested behavior.
	 */
	private void initalizeButtons() {
		initPreviousButton();
		initNeutralButton();
		initNextButton();
	}

	/**
	 * If this is the first time in (i.e. the saved instance state is null) we initialize the state machine from the extras passed to the activity during
	 * {@link Activity#startActivity(android.content.Intent, Bundle)}.
	 * 
	 * @param extras
	 */
	protected void initializeFromExtras(Bundle extras) {
		@SuppressWarnings("unchecked")
		Class<? extends StateFragment> clazz = (Class<? extends StateFragment>) extras.getSerializable(FIRST_STATE_CLASS);
		Log.i(TAG, "first state class is " + clazz.getName());

		Bundle args = new Bundle(extras);
		args.remove(FIRST_STATE_CLASS);

		StateDefinition def = new StateDefinition(clazz, args);

		getBasicDataFromBundle(extras);

		Log.i(TAG, "first state class is " + clazz.getName());
		moveForwardToState(def.clazz, def.args);
	}

	/**
	 * Rebuild the fragment stack by creating the fragments using the stored data.
	 * 
	 * @param savedState
	 *            The state from {@link #onCreate(Bundle)}. Cannot be null.
	 */
	protected void initializeFromSavedInstanceState(Bundle savedState) {
		Log.d(TAG, "restoring bundle " + savedState);

		@SuppressWarnings("unchecked")
		Stack<Class<? extends StateFragment>> classStack = (Stack<Class<? extends StateFragment>>) savedState.getSerializable(SAVED_CLASS_STACK);
		if (classStack == null || classStack.size() == 0) {
			throw new IllegalStateException("Cannot restore an empty class stack: " + classStack);
		}

		@SuppressWarnings("unchecked")
		Stack<Bundle> dataStack = (Stack<Bundle>) savedState.getSerializable(SAVED_DATA_STACK);
		if (dataStack == null || dataStack.size() == 0) {
			throw new IllegalStateException("Cannot restore an empty data stack: " + dataStack);
		}

		if (dataStack.size() != classStack.size()) {
			throw new IllegalStateException("Data stack (" + dataStack + ") and class stack (" + classStack + ") are different sizes");
		}

		getBasicDataFromBundle(savedState);

		buildStates(classStack, dataStack);
	}

	/**
	 * Get the label and initialize the click listener for the neutral button.
	 */
	private void initNeutralButton() {
		String label = getStates().peek().getNeutralButtonLabel();
		if (label == null) {
			neutral.setVisibility(allowNeutralPlaceholder ? View.INVISIBLE : View.GONE);
		} else {
			neutral.setVisibility(View.VISIBLE);
			neutral.setText(label);
			neutral.setEnabled(true);
		}

		neutral.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getStates().peek().onNeutralButtonClicked();
			}
		});
	}

	/**
	 * Get the label and initialize the click listener for the next button.
	 */
	private void initNextButton() {
		if (getStates().peek().isFinal()) {
			initNextButtonAsFinal();
		} else {
			initNextButtonAsNext();
		}
	}

	/**
	 * Sets up the next button as a final button in the wizard. If no text is provided by the state's {@link StateFragment#getNextButtonLabel()} then the value
	 * of R.string.wizard_finish will be used. The on click listener is set to close this activity.
	 */
	private void initNextButtonAsFinal() {
		String label = getStates().peek().getNextButtonLabel();
		if (label == null) {
			label = getString(R.string.wizard_finish);
		} else {
			next.setText(label);
		}

		next.setEnabled(getStates().peek().canGoForward());

		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleFinishClicked();
			}
		});
	}

	/**
	 * Sets up the next button as a next button in the wizard. If no text is provided by the state's {@link StateFragment#getNextButtonLabel()} then the value
	 * of R.string.wizard_next will be used. The on click listener is set to close this activity.
	 */
	private void initNextButtonAsNext() {
		String label = getStates().peek().getNextButtonLabel();
		if (label == null) {
			label = getString(R.string.wizard_next);
		} else {
			next.setText(label);
			next.setEnabled(getStates().peek().canGoForward());
		}

		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleNextClicked();
			}
		});
	}

	/**
	 * Get the label and initialize the click listener for the next button.
	 */
	private void initPreviousButton() {
		String label = getStates().peek().getPreviousButtonLabel();
		if (label == null) {
			previous.setVisibility(allowPreviousPlaceholder ? View.INVISIBLE : View.GONE);
		} else {
			previous.setVisibility(View.VISIBLE);
			previous.setText(label);
			previous.setEnabled(getStates().size() > 1);
		}

		previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleBackClicked();
			}
		});
	}

	/**
	 * Get the title (if any) from the state.
	 * 
	 * @param state
	 */
	abstract protected void initTitle();

	/**
	 * Takes the state class and bundles and builds the {@link StateFragment} using {@link Fragment#instantiate(android.content.Context, String, Bundle)}. That
	 * state is then loaded up using {@link #showState(StateFragment)}.
	 * 
	 * @param stateClass
	 * @param extras
	 */
	private void moveForwardToState(Class<? extends StateFragment> stateClass, Bundle extras) {
		Log.i(TAG, "moving forward to state " + stateClass.getName());

		if (stateClass == null || !StateFragment.class.isAssignableFrom(stateClass)) {
			throw new IllegalArgumentException("state class cannot be null and must extend StateFragment");
		}

		StateFragment state = StateFragment.create(this, stateClass.getName(), extras);

		addState(state);

		showState(state);
	}

	/**
	 * Swallow the back button event if the wizard has already been started. If we're on the first step set the result to RESULT_CANCELLED and exit the wizard.
	 */
	@Override
	public void onBackPressed() {
		if (getStates().size() > 1) {
			// Do Not goBackAStep();
		} else {
			// we're on the first step, cancel out
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getContentViewId());

		FragmentManager fm = getSupportFragmentManager();
		taskFragment = (TaskFragment) fm.findFragmentByTag(TASK_FRAGMENT_TAG);

		/**
		 * If the Fragment is non-null, then it is currently being retained across a configuration change.
		 */
		if (taskFragment == null) {
			taskFragment = new TaskFragment();
			fm.beginTransaction().add(taskFragment, TASK_FRAGMENT_TAG).commit();
		}

		/**
		 * Initialize the UI elements.
		 */
		title = (TextView) findViewById(R.id.wizard_title);
		stepCountTextView = (TextView) findViewById(R.id.wizard_step_count);
		previous = (Button) findViewById(R.id.wizard_previous_button);
		neutral = (Button) findViewById(R.id.wizard_neutral_button);
		next = (Button) findViewById(R.id.wizard_next_button);

		/**
		 * Finish initialization based on user preferences as set in the extras or stored in the saved instance state.
		 */
		if (savedInstanceState != null) {
			initializeFromSavedInstanceState(savedInstanceState);
		} else {
			initializeFromExtras(getIntent().getExtras());
		}
	}

	/**
	 * Gets the content view id to use for this wizard's main activity.
	 * 
	 * @see #onCreate(Bundle) onCreate(Bundle) for the UI elements it must contain.
	 * 
	 * @return The ID of the view, which must be a member of R.layout.
	 */
	abstract protected int getContentViewId();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// No menu in a wizard.
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		Log.d(TAG, "saving state");
		Stack<Class<? extends StateFragment>> classStack = new Stack<Class<? extends StateFragment>>();
		Stack<Bundle> dataStack = new Stack<Bundle>();
		for (StateFragment state : getStates()) {
			Class<? extends StateFragment> clazz = state.getClass();
			classStack.push(clazz);
			dataStack.push(state.getSavedInstanceState());
		}

		Log.i(TAG, "class stack: " + classStack);
		bundle.putSerializable(SAVED_CLASS_STACK, classStack);

		Log.i(TAG, "data stack: " + dataStack);
		bundle.putSerializable(SAVED_DATA_STACK, dataStack);

		bundle.putBoolean(ALLOW_NEUTRAL_BUTTON_PLACEHOLDER, allowNeutralPlaceholder);
		bundle.putBoolean(ALLOW_PREVIOUS_BUTTON_PLACEHOLDER, allowPreviousPlaceholder);
		bundle.putInt(STEP_COUNT, getStepCount());

		Log.d(TAG, "saved bundle " + bundle);
		super.onSaveInstanceState(bundle);
	}

	/**
	 * Initialize the {@link StateFragment} and load it up. This method moves forward, meaning it adds the state to the history. See {@link moveBack} for when a
	 * state is being popped.
	 * 
	 * @param state
	 *            the state we're moving to. It should be the top state in the stack.
	 */
	private void showState(StateFragment state) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment sf = fragmentManager.findFragmentByTag(state.getClass().getName());

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (sf != null) {
			Log.i(TAG, "found fragment " + sf.getClass().getName());
			fragmentTransaction.detach(sf);
		}
		fragmentTransaction.replace(R.id.wizard_content_container, state, state.getClass().getName());
		fragmentTransaction.commit();

		state.onAdded();

		initTitle();
		initalizeButtons();
	}

	@Override
	public void onPreExecute() {
		/**
		 * Display the progress dialog.
		 */
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Starting");

		FragmentManager fragmentManager = getSupportFragmentManager();
		ProgressDialogFragment newFragment = new ProgressDialogFragment();
		newFragment.show(fragmentManager, "Dialog");

		/**
		 * Disable all of the buttons so they can't navigate while the background task is running.
		 */
		previous.setEnabled(false);
		neutral.setEnabled(false);
		next.setEnabled(false);
	}

	@Override
	public void onProgressUpdate(String... progress) {
		/**
		 * Runs when the user calls
		 */
		progressDialog.setMessage(progress[0]);
	}

	@Override
	public void onCancelled() {
		/**
		 * Revert to the normal state (no progress dialog, UI elements based on state preferences.
		 */
		progressDialog.dismiss();
		initalizeButtons();
	}

	@Override
	public void onPostExecute(Boolean result) {
		/**
		 * Hide the dialog and handle success or failure.
		 */
		progressDialog.dismiss();
		if (result) {
			handleSuccessfulValidation();
		} else {
			initalizeButtons();
		}

	}

	/**
	 * @return the current stack of {@link StateFragment}s.
	 */
	protected Stack<StateFragment> getStates() {
		return states;
	}

	/**
	 * @return the {@link TextView} for the title.
	 */
	public TextView getTitleView() {
		return title;
	}

	/**
	 * @return the {@link TextView} for the step count.
	 */
	public TextView getStepCountTextView() {
		return stepCountTextView;
	}

	/**
	 * @return the number of steps in the current fragment, which may be STEP_COUNT_UNKNOWN.
	 */
	public int getStepCount() {
		return stepCount;
	}

	/**
	 * Maintains a static progress dialog so orientation changes don't automatically destroy it when background validation is underway. The dialog is originally
	 * shown with no title or text. States performing background validation can update the text via {@link AsyncTask#publishProgress
	 * ValidationAsyncTask.publishProgress(String)}.
	 */
	public static class ProgressDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			progressDialog = ProgressDialog.show(getActivity(), "", "");
			return progressDialog;
		}
	}
}
