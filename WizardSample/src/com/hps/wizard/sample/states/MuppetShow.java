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
public class MuppetShow extends StateFragment {

	private static final String CHECKED_BUTTON_ID = "checkedId";
	private static final String TAG = "MuppetShow";
	private RadioGroup choiceGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_state_muppets, container, false);

		choiceGroup = (RadioGroup) v.findViewById(R.id.choiceGroup);

		/**
		 * Set up a listener to enable or disable the next button.
		 */
		OnCheckedChangeListener thingListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (getWizardActivity() != null) {
					getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON,
							choiceGroup.getCheckedRadioButtonId() != -1, MuppetShow.this);
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
		return "It's Time to Play the Music";
	}

	@Override
	public boolean canGoForward() {
		/**
		 * We can go forward only if the user has selected an answer.
		 */
		return choiceGroup != null && choiceGroup.getCheckedRadioButtonId() != -1;
	}

	@Override
	public StateDefinition getNextState() {
		/**
		 * The next state is always the results.
		 */
		StateDefinition def = new StateDefinition(Results.class, null);
		return def;
	}

	@Override
	public void onAdded() {
		Log.i(TAG, "onAdded, getArguments = " + getArguments());
		super.onAdded();

		/**
		 * Always disable the next button when we first start.
		 */
		getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON, false, MuppetShow.this);
	}

	@Override
	public Bundle getSavedInstanceState() {
		Bundle bundle = new Bundle();
		/**
		 * Store off which radio button was checked (if any).
		 */
		if (choiceGroup != null) {
			bundle.putInt(CHECKED_BUTTON_ID, choiceGroup.getCheckedRadioButtonId());
		}
		return bundle;
	}

	@Override
	public void onForward() {
		super.onForward();

		/**
		 * When the user moves forward, store their answer.
		 */
		int id = choiceGroup.getCheckedRadioButtonId();
		RadioButton btn = (RadioButton) choiceGroup.findViewById(id);
		String label = btn.getText().toString();
		SharedPreferences prefs = getWizardActivity().getSharedPreferences("answers", Context.MODE_PRIVATE);
		prefs.edit().putString("muppet_answer", label).putBoolean("muppet_correct", label.equals("The Swedish Chef")).commit();
	}

	@Override
	public void onBack() {
		super.onBack();

		/**
		 * When the user moves back, clear any stored answer.
		 */
		SharedPreferences prefs = getWizardActivity().getSharedPreferences("answers", Context.MODE_PRIVATE);
		prefs.edit().remove("muppet_answer").remove("muppet_correct").commit();
	}

}
