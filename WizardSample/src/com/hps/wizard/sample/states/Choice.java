package com.hps.wizard.sample.states;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.hps.wizard.AbstractWizardActivity;
import com.hps.wizard.StateFragment;
import com.hps.wizard.sample.R;

/**
 * This state forces the user to pick between the Muppet Show and Sesame Street. The next state will be {@link AreYouSure}, built according to the choice here.
 * It also demonstrates how to react to a press of the neutral button in {@link #onNeutralButtonClicked()} and {@link #validate()}.
 */
public class Choice extends StateFragment {

	private static final String CHECKED_BUTTON_ID = "checkedId";
	private static final String TAG = "Choice";
	private static final String FIGHT_CLICKED = "fc";
	private static final String IMAGE_VISIBILITY = "iv";
	private static final String DESCRIPTION_VISIBILITY = "dv";
	private RadioGroup choiceGroup;
	private ImageView imageView;
	private boolean fightClicked;
	private TextView descriptiveText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_state_choice, container, false);

		choiceGroup = (RadioGroup) v.findViewById(R.id.choiceGroup);
		imageView = (ImageView) v.findViewById(R.id.imageView);
		descriptiveText = (TextView) v.findViewById(R.id.descriptiveText);

		/**
		 * Set up a listener to enable or disable the next button.
		 */
		OnCheckedChangeListener thingListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (getWizardActivity() != null) {
					getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON, group.getCheckedRadioButtonId() != -1, Choice.this);
				}
			}
		};
		choiceGroup.setOnCheckedChangeListener(thingListener);

		/**
		 * Restore the state if we're rebuilding the screen after orientation change.
		 */
		if (getArguments() != null) {
			int checkedId = getArguments().getInt(CHECKED_BUTTON_ID, -1);
			if (checkedId != -1) {
				choiceGroup.check(checkedId);
			}

			fightClicked = getArguments().getBoolean(FIGHT_CLICKED);
			imageView.setVisibility(getArguments().getInt(IMAGE_VISIBILITY));
			descriptiveText.setVisibility(getArguments().getInt(DESCRIPTION_VISIBILITY));
		}

		return v;
	}

	@Override
	public String getTitle() {
		return "Fight!";
	}

	@Override
	public String getPreviousButtonLabel() {
		return getWizardActivity().getString(R.string.wizard_previous);
	}

	@Override
	public boolean validate() {
		/**
		 * Pop an alert dialog and don't let them move forward unless they've clicked the Fight! button.
		 */
		if (!fightClicked) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Validation Error");
			builder.setMessage("You can't move forward without clicking \"Fight!\"");
			builder.setPositiveButton("Ok", null);
			builder.create().show();
			return false;
		}

		return true;
	}

	@Override
	public boolean canGoForward() {
		/**
		 * They can go forward if a choice has been made, but see also {@link validate()}.
		 */
		return choiceGroup != null && choiceGroup.getCheckedRadioButtonId() != -1;
	}

	@Override
	public StateDefinition getNextState() {
		Bundle args = new Bundle();
		/**
		 * Build an AreYouSure state who yes sends them to their choice and whose no sends them to the other.
		 */
		switch (choiceGroup.getCheckedRadioButtonId()) {
		case R.id.sesameStreet:
			args.putString(AreYouSure.ARE_YOU_SURE_TEXT,
					"Even Kermit the Frog left Sesame Street for the Muppet Show. And it was the best choice of his career.");
			args.putSerializable(AreYouSure.YES_STATE, SesameStreet.class);
			args.putSerializable(AreYouSure.NO_STATE, MuppetShow.class);
			break;
		case R.id.muppets:
			args.putString(AreYouSure.ARE_YOU_SURE_TEXT, "Without Sesame Street there would be no Kermit, and thus no Muppet Show.");
			args.putSerializable(AreYouSure.YES_STATE, MuppetShow.class);
			args.putSerializable(AreYouSure.NO_STATE, SesameStreet.class);
			break;
		default:
			break;
		}

		StateDefinition def = new StateDefinition(AreYouSure.class, args);
		return def;
	}

	@Override
	public void onAdded() {
		Log.i(TAG, "onAdded, getArguments = " + getArguments());
		super.onAdded();

		/**
		 * always disable the next button when we're first added.
		 */
		getWizardActivity().enableButton(AbstractWizardActivity.NEXT_BUTTON, false, Choice.this);
	}

	@Override
	public Bundle getSavedInstanceState() {
		Bundle bundle = new Bundle();
		/**
		 * Save the current choice.
		 */
		if (choiceGroup != null) {
			bundle.putInt(CHECKED_BUTTON_ID, choiceGroup.getCheckedRadioButtonId());
		}

		/**
		 * Save the state of the fight button and the views it toggles.
		 */
		bundle.putBoolean(FIGHT_CLICKED, fightClicked);
		bundle.putInt(IMAGE_VISIBILITY, imageView.getVisibility());
		bundle.putInt(DESCRIPTION_VISIBILITY, descriptiveText.getVisibility());
		return bundle;
	}

	@Override
	public String getNeutralButtonLabel() {
		return "Fight!";
	}

	@Override
	public void onNeutralButtonClicked() {
		/**
		 * Toggle the image and text when the fight button is clicked.
		 */
		if (imageView.getVisibility() == View.GONE) {
			imageView.setVisibility(View.VISIBLE);
			descriptiveText.setVisibility(View.GONE);
		} else {
			imageView.setVisibility(View.GONE);
			descriptiveText.setVisibility(View.VISIBLE);
		}

		/**
		 * Also, mark the button as clicked so {@link validate()} will pass.
		 */
		fightClicked = true;
	}
}
