package com.hps.wizard.sample.states;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.hps.wizard.StateFragment;
import com.hps.wizard.AbstractWizardActivity;
import com.hps.wizard.sample.R;

/**
 * A state offering a single multiple choice question. The answer supplied and whether it is right or wrong is stored in {@link SharedPreferences} when the user
 * moves forward and cleared if they go back.
 */
public class Seuss extends StateFragment {

	private static final String CHECKED_BUTTON_ID = "checkedId";
	private static final String TAG = "Seuss";
	private RadioGroup thingGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_state_seuss, container, false);

		thingGroup = (RadioGroup) v.findViewById(R.id.things);

		/**
		 * Set up a listener to enable or disable the next button.
		 */
		OnCheckedChangeListener thingListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (getWizardActivity() != null) {
					getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON,
							thingGroup.getCheckedRadioButtonId() != -1, Seuss.this);
				}
			}
		};
		thingGroup.setOnCheckedChangeListener(thingListener);

		/**
		 * Check the correct radio button if we're rebuilding the screen after orientation change.
		 */
		if (getArguments() != null) {
			int checkedId = getArguments().getInt(CHECKED_BUTTON_ID, -1);
			if (checkedId != -1) {
				thingGroup.check(checkedId);
			}
		}

		return v;
	}

	@Override
	public String getTitle() {
		return "The Simplest Seuss For Youngest Use";
	}

	@Override
	public String getPreviousButtonLabel() {
		return "BG Validation";
	}

	@Override
	public boolean canGoForward() {
		/**
		 * We can go forward only if the user has selected an answer.
		 */
		return thingGroup != null && thingGroup.getCheckedRadioButtonId() != -1;
	}

	@Override
	public StateDefinition getNextState() {
		/**
		 * The next state is always the Choice.
		 */
		StateDefinition def = new StateDefinition(Choice.class, null);
		return def;
	}

	@Override
	public void onAdded() {
		Log.i(TAG, "onAdded, getArguments = " + getArguments());
		super.onAdded();

		/**
		 * Always disable the next button when we first start.
		 */
		getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON, false, Seuss.this);
	}

	@Override
	public Bundle getSavedInstanceState() {
		Bundle bundle = new Bundle();
		/**
		 * Store off which radio button was checked (if any).
		 */
		if (thingGroup != null) {
			bundle.putInt(CHECKED_BUTTON_ID, thingGroup.getCheckedRadioButtonId());
		}
		return bundle;
	}

	@Override
	public void onForward() {
		super.onForward();

		/**
		 * When the user moves forward, store their answer.
		 */
		int id = thingGroup.getCheckedRadioButtonId();
		RadioButton btn = (RadioButton) thingGroup.findViewById(id);
		String label = btn.getText().toString();
		SharedPreferences prefs = getWizardActivity().getSharedPreferences("answers", Context.MODE_PRIVATE);
		prefs.edit().putString("seuss_answer", label).putBoolean("seuss_correct",
				label.equals("Thing 1") || label.equals("Thing 2")).commit();
	}

	@Override
	public void onBack() {
		super.onBack();

		/**
		 * When the user moves back, clear any stored answer.
		 */
		SharedPreferences prefs = getWizardActivity().getSharedPreferences("answers", Context.MODE_PRIVATE);
		prefs.edit().remove("seuss_answer").remove("seuss_correct").commit();
	}

}
