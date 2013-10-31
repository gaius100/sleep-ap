/**
 * Copyright (c) 2013, J. Behar, A. Roebuck, M. Shahid, J. Daly, A. Hallack, 
 * N. Palmius, K. Niehaus, G. Clifford (University of Oxford). All rights reserved.
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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private EditTextPreference numberRecordingsPreference;
	private EditTextPreference macAddressPreference;
	private EditTextPreference graphsPreference;
	private EditTextPreference recordingDelayPreference;
	private EditTextPreference recordingDurationPreference;
	private CheckBoxPreference earlyExitDeletionPreference;
	private CheckBoxPreference checkSpaceandbatteryPreference;
	private CheckBoxPreference writeLogPreference;
	private ListPreference odiThresholdPreference;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Have to use deprecated methods because we're supporting versions of
		// Android before API 11 (3.0).
		addPreferencesFromResource(R.xml.preferences);
		numberRecordingsPreference = (EditTextPreference) getPreferenceScreen().findPreference(Constants.PREF_NUMBER_RECORDINGS);
		macAddressPreference = (EditTextPreference) getPreferenceScreen().findPreference(Constants.PREF_MAC_ADDRESS);
		graphsPreference = (EditTextPreference) getPreferenceScreen().findPreference(Constants.PREF_GRAPH_SECONDS);
		recordingDelayPreference = (EditTextPreference) getPreferenceScreen().findPreference(Constants.PREF_RECORDING_START_DELAY);
		recordingDurationPreference = (EditTextPreference) getPreferenceScreen().findPreference(Constants.PREF_RECORDING_DURATION);
		earlyExitDeletionPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(Constants.PREF_EARLY_EXIT_DELETION);
		checkSpaceandbatteryPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(Constants.PREF_CHECK_SPACEANDBATTERY);
		writeLogPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(Constants.PREF_WRITE_LOG);
		odiThresholdPreference = (ListPreference) getPreferenceScreen().findPreference(Constants.PREF_ODI_THRESHOLD);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up initial values.
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		numberRecordingsPreference.setSummary(getString(R.string.recordingsPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_NUMBER_RECORDINGS, Constants.DEFAULT_NUMBER_RECORDINGS));
		macAddressPreference.setSummary(getString(R.string.macAddressPreferencesSummary) + " " + sharedPreferences.getString(Constants.PREF_MAC_ADDRESS, Constants.DEFAULT_MAC_ADDRESS));
		graphsPreference.setSummary(getString(R.string.graphPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS, Constants.DEFAULT_GRAPH_RANGE));
		odiThresholdPreference.setSummary(getString(R.string.odiThresholdPreferenceSummary) + " " + odiThresholdPreference.getValue() + "%%");
		recordingDelayPreference.setSummary(getString(R.string.recordingDelayPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_RECORDING_START_DELAY, Constants.DEFAULT_RECORDING_START_DELAY));
		recordingDurationPreference.setSummary(getString(R.string.recordingDurationPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_RECORDING_DURATION, Constants.DEFAULT_RECORDING_DURATION));
		// Set up a listener for key changes.
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	};

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister listener.
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	};

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Constants.PREF_NUMBER_RECORDINGS)) {
			numberRecordingsPreference.setSummary(getString(R.string.recordingsPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_NUMBER_RECORDINGS, Constants.DEFAULT_NUMBER_RECORDINGS));
		} else if (key.equals(Constants.PREF_MAC_ADDRESS)) {
			macAddressPreference.setSummary(getString(R.string.macAddressPreferencesSummary) + " " + sharedPreferences.getString(Constants.PREF_MAC_ADDRESS, Constants.DEFAULT_MAC_ADDRESS));
		} else if (key.equals(Constants.PREF_GRAPH_SECONDS)) {
			graphsPreference.setSummary(getString(R.string.graphPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS, Constants.DEFAULT_GRAPH_RANGE));
		} else if (key.equals(Constants.PREF_ODI_THRESHOLD)) {
			odiThresholdPreference.setSummary(getString(R.string.odiThresholdPreferenceSummary) + " " + odiThresholdPreference.getValue() + "%%");
		} else if (key.equals(Constants.PREF_RECORDING_START_DELAY)) {
			String val = sharedPreferences.getString(Constants.PREF_RECORDING_START_DELAY, Constants.DEFAULT_RECORDING_START_DELAY);
			if (val != null && !val.equals("")) {
				int candidate = Integer.parseInt(val);
				if (candidate < 0) {
					recordingDelayPreference.setText("0");
				}
			} else {
				recordingDelayPreference.setText("0");
			}
			recordingDelayPreference.setSummary(getString(R.string.recordingDelayPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_RECORDING_START_DELAY, Constants.DEFAULT_RECORDING_START_DELAY));
		} else if (key.equals(Constants.PREF_RECORDING_DURATION)) {
			String val = sharedPreferences.getString(Constants.PREF_RECORDING_DURATION, Constants.DEFAULT_RECORDING_DURATION);
			if (val != null && !val.equals("")) {
				int candidate = Integer.parseInt(val);
				if (candidate < 1) {
					recordingDurationPreference.setText("1");
				}
			} else {
				recordingDurationPreference.setText("1");
			}
			recordingDurationPreference.setSummary(getString(R.string.recordingDurationPreferenceSummary) + " " + sharedPreferences.getString(Constants.PREF_RECORDING_DURATION, Constants.DEFAULT_RECORDING_DURATION));
		} else if (key.equals(Constants.PREF_ADVANCED_USER)) {
			// If the user has just turned off advanced settings, restore them to their defaults.
			if (!sharedPreferences.getBoolean(Constants.PREF_ADVANCED_USER, Constants.DEFAULT_ADVANCED_USER)) {
				recordingDelayPreference.setText(Constants.DEFAULT_RECORDING_START_DELAY);
				recordingDurationPreference.setText(Constants.DEFAULT_RECORDING_DURATION);
				earlyExitDeletionPreference.setChecked(Constants.DEFAULT_EARLY_EXIT_DELETION);
				checkSpaceandbatteryPreference.setChecked(Constants.DEFAULT_CHECK_SPACEANDBATTERY);
				writeLogPreference.setChecked(Constants.DEFAULT_WRITE_LOG);
				odiThresholdPreference.setValue(Constants.DEFAULT_ODI_THRESHOLD);
			}
		}
	}
}
