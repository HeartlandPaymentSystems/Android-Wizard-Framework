package com.hps.wizard.sample.states;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hps.wizard.StateFragment;
import com.hps.wizard.sample.R;

/**
 * Nothing really special here except that the name supplied in the arguments is used to build the title.
 */
public class Instructions extends StateFragment {

	public static final String NAME_ARG = "name";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_state_instructions, container, false);

		TextView tv = (TextView) v.findViewById(R.id.textView);
		CharSequence styledText = Html.fromHtml(getString(R.string.nice_html_instructions));
		tv.setText(styledText);

		return v;
	}

	@Override
	public String getTitle() {
		/**
		 * Get the name from the state's arguments.
		 */
		return "Hi " + getArguments().getString(NAME_ARG) + "!";
	}

	@Override
	public String getPreviousButtonLabel() {
		return null;
	}

	@Override
	public StateDefinition getNextState() {
		/**
		 * When they move to the next state, clear out any prior test results.
		 */
		SharedPreferences prefs = getWizardActivity().getSharedPreferences("answers", Context.MODE_PRIVATE);
		prefs.edit().clear().commit();

		/**
		 * The next state is always BackgroundValidation.
		 */
		StateDefinition def = new StateDefinition(BackgroundValidation.class, null);

		return def;
	}

}
