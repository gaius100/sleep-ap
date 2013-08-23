/**
 * Copyright (c) 2013, J. Behar, A. Roebuck, M. Shahid, J. Daly, A. Hallack, 
 * N. Palmius, G. Clifford (University of Oxford). All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 	1. 	Redistributions of source code must retain the above copyright notice, this 
 * 		list of conditions and the following disclaimer.
 * 	2.	Redistributions in binary form must reproduce the above copyright notice, 
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * NOT MEDICAL SOFTWARE.
 * 
 * This software is provided for informational or research purposes only, and is not
 * for professional medical use, diagnosis, treatment or care, nor is it intended to
 * be a substitute therefor. Always seek the advice of a physician or other qualified
 * health provider properly licensed to practice medicine or general healthcare in
 * your jurisdiction concerning any questions you may have regarding any health
 * problem. Never disregard professional medical advice or delay in seeking it
 * because of something you have observed through the use of this software. Always
 * consult with your physician or other qualified health care provider before
 * embarking on a new treatment, diet or fitness programme.
 * 
 * Graphical charts copyright (c) AndroidPlot (http://androidplot.com/), SVM 
 * component copyright (c) LIBSVM (http://www.csie.ntu.edu.tw/~cjlin/libsvm/) - all 
 * rights reserved.
 * */

package ibme.sleepap;

import ibme.sleepap.analysis.RecordingsList;
import ibme.sleepap.education.OsaQuestions;
import ibme.sleepap.history.ViewHistory;
import ibme.sleepap.recording.ChooseSignals;
import ibme.sleepap.screening.StopBangQuestionnaire;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends SleepApActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent sendingIntent = getIntent();
		boolean cameFromMainMenuButton = sendingIntent.getBooleanExtra(Constants.EXTRA_HIDE_LICENCE, false);

		if (!cameFromMainMenuButton) {
			// Set up dialog box for licence.
			final Dialog dialog = new Dialog(MainMenu.this);
			dialog.setContentView(R.layout.welcome_dialog);
			dialog.setTitle(getString(R.string.welcome));
			TextView text = (TextView) dialog.findViewById(R.id.about);
			text.setText(Html.fromHtml(getString(R.string.aboutUsAlertMessage)));
			text.setMovementMethod(LinkMovementMethod.getInstance());
			Button button = (Button) dialog.findViewById(R.id.okButton);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainMenu.this);
					boolean isFirstTimeAppLaunched = sharedPreferences.getBoolean(Constants.PREF_FIRST_LAUNCH, true);
					if (isFirstTimeAppLaunched) {
						dialog.dismiss();
						sharedPreferences.edit().putBoolean(Constants.PREF_FIRST_LAUNCH, false).commit();
						Intent intent = new Intent(MainMenu.this, Tour.class);
						startActivity(intent);
					} else {
						dialog.dismiss();
					}
				}
			});
			Display display = getWindowManager().getDefaultDisplay();
			@SuppressWarnings("deprecation")
			int width = display.getWidth(); // deprecated
			dialog.getWindow().setLayout(width - Utils.dipToPixels(30, this), LayoutParams.WRAP_CONTENT);
			dialog.show();
		}

		Button learnButton = (Button) findViewById(R.id.learnButton);
		Button questionnaireButton = (Button) findViewById(R.id.questionnaireButton);
		Button recordButton = (Button) findViewById(R.id.recordButton);
		Button analyseButton = (Button) findViewById(R.id.analyseButton);
		Button previousButton = (Button) findViewById(R.id.previousButton);

		learnButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open the LearnAbout activity.
				Intent intent = new Intent(MainMenu.this, OsaQuestions.class);
				startActivity(intent);
			}
		});

		questionnaireButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open the STOP-BANG questionnaire first activity.
				Intent intent = new Intent(MainMenu.this, StopBangQuestionnaire.class);
				startActivity(intent);
			}
		});

		recordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start the screening module using Audio and accelerometer
				Intent intent = new Intent(MainMenu.this, ChooseSignals.class);
				startActivity(intent);
			}
		});

		analyseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start the analysis module.
				Intent intent = new Intent(MainMenu.this, RecordingsList.class);
				startActivity(intent);
			}
		});

		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, ViewHistory.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	/** Handles option selection. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Override SleepApActivity's menu handling to stop "Main Menu"
		// needlessly launching again.
		if (item.getItemId() == R.id.menu_exit) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}