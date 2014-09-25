package com.hps.wizard.sample.states;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hps.wizard.StateFragment;
import com.hps.wizard.sample.R;

/**
 * This class gathers up the answers from prior steps and display a summary to the user. Because they were store in shared preferences there's no need to send
 * them as bundled arguments.
 */
public class Results extends StateFragment {
	private int total;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_state_results, container, false);

		WebView wv = (WebView) v.findViewById(R.id.webView);

		StringBuilder html = getHtmlResults();

		wv.loadDataWithBaseURL(null, html.toString(), "text/html", "utf-8", null);

		return v;
	}

	/**
	 * @return an HTML string of the results built from the answers stored in the {@link SharedPreferences}.
	 */
	private StringBuilder getHtmlResults() {
		SharedPreferences prefs = getActivity().getSharedPreferences("answers", Context.MODE_PRIVATE);

		StringBuilder html = new StringBuilder();

		html.append("<html><body><table border='1' style='border-collapse : collapse; border : 1px solid black;'><tr><th valign='top'align='left'  width='50%'>Question</th><th valign='top'align='left'  width='20%'>Your Answer</th><th valign='top'align='left'  width='20%'>Correct Answer</th><th valign='top' align='right' width='10%'>Score</th></tr>");

		addSeussAnswer(html, prefs);
		if (prefs.getString("muppet_answer", null) != null) {
			addMuppetAnswer(html, prefs);
		} else {
			addSesameStreetAnswer(html, prefs);
		}

		addTotal(html);

		html.append("</table></body></html>");

		return html;
	}

	/**
	 * Total the correct answers and add a line to the results.
	 * 
	 * @param html
	 */
	private void addTotal(StringBuilder html) {
		html.append("<tr><td valign='top'colspan='3'>Total</td><td valign='top'  align='right'>");
		html.append(total);
		html.append("</td></tr>");
	}

	/**
	 * Add a line to the results for the Sesame Street question. Increment the score if they were right.
	 * 
	 * @param html
	 * @param prefs
	 */
	private void addSesameStreetAnswer(StringBuilder html, SharedPreferences prefs) {
		String answer = prefs.getString("sesame_answer", null);
		html.append("<tr><td valign='top'>Who owned the store until he died in 1982?</td><td valign='top'>");
		html.append(answer);
		html.append("</td><td valign='top'>Mr. Hooper</td><td valign='top'  align='right'>");

		int score = prefs.getBoolean("sesame_correct", false) ? 1 : 0;
		total += score;

		html.append(score);
		html.append("</td></tr>");
	}

	/**
	 * Add a line to the results for the Muppet Show question. Increment the score if they were right.
	 * 
	 * @param html
	 * @param prefs
	 */
	private void addMuppetAnswer(StringBuilder html, SharedPreferences prefs) {
		String answer = prefs.getString("muppet_answer", null);
		html.append("<tr><td valign='top'>Which character was played by both Jim Hensen and Frank Oz simultaneously?</td><td valign='top'>");
		html.append(answer);
		html.append("</td><td valign='top'>The Swedish Chef</td><td valign='top' align='right'>");

		int score = prefs.getBoolean("muppet_correct", false) ? 1 : 0;
		total += score;

		html.append(score);
		html.append("</td></tr>");
	}

	/**
	 * Add a line to the results for the Dr. Seuss question. Increment the score if they were right.
	 * 
	 * @param html
	 * @param prefs
	 */
	private void addSeussAnswer(StringBuilder html, SharedPreferences prefs) {
		String answer = prefs.getString("seuss_answer", null);
		html.append("<tr><td valign='top'>Which thing did the Cat bring?</td><td valign='top'>");
		html.append(answer);
		html.append("</td><td valign='top'>Thing 1 or Thing 2</td><td valign='top' align='right'>");

		int score = prefs.getBoolean("seuss_correct", false) ? 1 : 0;
		total += score;

		html.append(score);
		html.append("</td></tr>");
	}

	@Override
	public String getTitle() {
		return "Results";
	}

	@Override
	public StateDefinition getNextState() {
		/**
		 * There is never a next state. We're done.
		 */
		return null;
	}

	@Override
	public boolean isFinal() {
		/**
		 * This is the last state in the wizard.
		 */
		return true;
	}

	@Override
	public Intent getFinalResult() {
		/**
		 * Load an intent with the final results. This is only necessary if the original activity is waiting for a response.
		 */
		Intent intent = new Intent();
		intent.putExtra("score", total);
		return intent;
	}
}
