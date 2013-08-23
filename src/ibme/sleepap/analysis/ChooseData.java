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

package ibme.sleepap.analysis;

import ibme.sleepap.Constants;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class ChooseData extends SleepApActivity {

	private File accelerationFile, audioFile, ppgFile, spo2File, demoFile, recordingDir;
	private CheckBox actigraphyCheckBox, audioCheckBox, demographicsCheckBox, ppgCheckBox, spo2CheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_data);

		Intent callingIntent = getIntent();
		recordingDir = (File) callingIntent.getSerializableExtra(Constants.EXTRA_RECORDING_DIRECTORY);
		final File appDir = new File(Environment.getExternalStorageDirectory().getPath() + "/SleepAp/");

		audioCheckBox = (CheckBox) findViewById(R.id.opt_audio);
		actigraphyCheckBox = (CheckBox) findViewById(R.id.opt_actigraphy);
		demographicsCheckBox = (CheckBox) findViewById(R.id.opt_demographics);
		ppgCheckBox = (CheckBox) findViewById(R.id.opt_PPG);
		spo2CheckBox = (CheckBox) findViewById(R.id.opt_o2Sat);

		// Look for questionnaire file.
		File questionnaireDir = new File(appDir, Constants.FILENAME_QUESTIONNAIRE_DIRECTORY);
		for (File candidateFile : questionnaireDir.listFiles()) {

			String candidateFileName = candidateFile.getName();
			if (candidateFileName.equals(Constants.FILENAME_QUESTIONNAIRE)) {
				markCheckBoxAvailable(demographicsCheckBox);
				demoFile = candidateFile;
				continue;
			}
		}

		for (File candidateFile : recordingDir.listFiles()) {

			String candidateFileName = candidateFile.getName();
			if (candidateFileName.equals(Constants.FILENAME_ACCELERATION_PROCESSED)) {
				markCheckBoxAvailable(actigraphyCheckBox);
				accelerationFile = candidateFile;
				continue;
			}
			if (candidateFileName.equals(Constants.FILENAME_AUDIO_PROCESSED)) {
				markCheckBoxAvailable(audioCheckBox);
				audioFile = candidateFile;
				continue;
			}
			if (candidateFileName.equals(Constants.FILENAME_PPG)) {
				markCheckBoxAvailable(ppgCheckBox);
				ppgFile = candidateFile;
				continue;
			}
			if (candidateFileName.equals(Constants.FILENAME_SPO2)) {
				markCheckBoxAvailable(spo2CheckBox);
				spo2File = candidateFile;
				continue;
			}
		}

		// If user is not an 'advanced user' (as determined by the settings),
		// skip this data choice as they should use all data available.
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isAdvancedUser = sharedPreferences.getBoolean(Constants.PREF_ADVANCED_USER, false);
		if (!isAdvancedUser) {
			startAnalysisActivity();
			finish();
			overridePendingTransition(0, 0);
		}

		Button nextButton = (Button) findViewById(R.id.nextButton);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startAnalysisActivity();
			}
		});
	}

	protected void startAnalysisActivity() {
		Intent intent = new Intent(ChooseData.this, PerformAnalysis.class);
		if (actigraphyCheckBox.isChecked()) {
			intent.putExtra(Constants.EXTRA_ACTIGRAPHY_FILE, accelerationFile);
		}
		if (audioCheckBox.isChecked()) {
			intent.putExtra(Constants.EXTRA_AUDIO_FILE, audioFile);
		}
		if (demographicsCheckBox.isChecked()) {
			intent.putExtra(Constants.EXTRA_DEMOGRAPHICS_FILE, demoFile);
		}
		if (ppgCheckBox.isChecked()) {
			intent.putExtra(Constants.EXTRA_PPG_FILE, ppgFile);
		}
		if (spo2CheckBox.isChecked()) {
			intent.putExtra(Constants.EXTRA_SPO2_FILE, spo2File);
		}

		intent.putExtra(Constants.EXTRA_RECORDING_DIRECTORY, recordingDir);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);
	}

	private void markCheckBoxAvailable(CheckBox checkBox) {
		checkBox.setClickable(true);
		checkBox.setEnabled(true);
		checkBox.setChecked(true);
		checkBox.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.enteringfromleft, R.anim.exitingtoright);
	}
}