package com.hps.wizard.sample.states;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.hps.wizard.StateFragment;
import com.hps.wizard.AbstractWizardActivity;
import com.hps.wizard.sample.R;

/**
 * This fragment is a state fragment which accepts two possible next states and a label. It then moves forward to the appropriate state based on user selection.
 */
public class AreYouSure extends StateFragment {
	public static final String ARE_YOU_SURE_TEXT = "text";
	public static final String YES_STATE = "yesState";
	public static final String NO_STATE = "noState";
	private static final String CHECKED_BUTTON_ID = "checkedId";

	private RadioButton yes, no;
	private RadioGroup choiceGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_state_are_your_sure, container, false);

		Bundle args = getArguments();
		String message = args.getString(ARE_YOU_SURE_TEXT);
		TextView tv = (TextView) v.findViewById(R.id.textView);
		tv.setText(message);

		yes = (RadioButton) v.findViewById(R.id.yes);
		no = (RadioButton) v.findViewById(R.id.no);

		choiceGroup = (RadioGroup) v.findViewById(R.id.choiceGroup);

		/**
		 * Set up a listener to enable or disable the next button.
		 */
		OnCheckedChangeListener thingListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (getWizardActivity() != null) {
					getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON, group.getCheckedRadioButtonId() != -1,
							AreYouSure.this);
				}
			}
		};
		choiceGroup.setOnCheckedChangeListener(thingListener);

		/**
		 * Check the correct radio button if we're rebuilding the screen after orientation change.
		 */
		if (getArguments() != null) {
			int checkedId = getArguments().getInt(CHECKED_BUTTON_ID, -1);
			if (checkedId != -1) {
				choiceGroup.check(checkedId);
			}
		}

		return v;
	}

	@Override
	public String getTitle() {
		return "Are You Sure?";
	}

	@Override
	public String getPreviousButtonLabel() {
		return getWizardActivity().getString(R.string.wizard_previous);
	}

	@Override
	public boolean canGoForward() {
		/**
		 * We can go forward only if the fragment has been created and the user has selected yes or no.
		 */
		return yes != null && (yes.isChecked() || no.isChecked());
	}

	@SuppressWarnings("unchecked")
	@Override
	public StateDefinition getNextState() {
		Bundle args = getArguments();
		Class<? extends StateFragment> nextState;
		/**
		 * Select the correct state based on which radio button is checked.
		 */
		if (yes.isChecked()) {
			nextState = (Class<? extends StateFragment>) args.getSerializable(YES_STATE);
		} else {
			nextState = (Class<? extends StateFragment>) args.getSerializable(NO_STATE);
		}

		return new StateDefinition(nextState, null);
	}

	@Override
	public Bundle getSavedInstanceState() {
		Bundle bundle = getBundleToSave();
		/**
		 * Store off which radio button was checked (if any).
		 */
		if (choiceGroup != null) {
			bundle.putInt(CHECKED_BUTTON_ID, choiceGroup.getCheckedRadioButtonId());
		}
		return bundle;
	}

}
