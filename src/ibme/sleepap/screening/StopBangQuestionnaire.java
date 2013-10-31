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

package ibme.sleepap.screening;

import ibme.sleepap.Constants;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StopBangQuestionnaire extends SleepApActivity {

	private enum HeightUnit {
		Metres,
		FeetInches
	}

	private enum WeightUnit {
		Kilograms, 
		StonePounds,
		Pounds
	}

	private enum NeckSizeUnit {
		Inches,
		Centimetres
	}
	
	private HeightUnit selectedHeightUnit;
	private WeightUnit selectedWeightUnit;
	private NeckSizeUnit selectedNeckSizeUnit;

	private EditText height1;
	private EditText height2;
	private EditText weight1;
	private EditText weight2;
	private EditText age;
	private EditText neckSize;
	private RadioGroup stopBang1;
	private RadioGroup stopBang2;
	private RadioGroup stopBang3;
	private RadioGroup stopBang4;
	private RadioGroup stopBang8;
	private Spinner heightUnitSpinner;
	private Spinner weightUnitSpinner;
	private Spinner neckSizeUnitSpinner;
	private Spinner ethnicitySpinner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_bang_questionnaire);

		stopBang1 = (RadioGroup) findViewById(R.id.stopbang1);
		stopBang2 = (RadioGroup) findViewById(R.id.stopbang2);
		stopBang3 = (RadioGroup) findViewById(R.id.stopbang3);
		stopBang4 = (RadioGroup) findViewById(R.id.stopbang4);
		height1 = (EditText) findViewById(R.id.heightentry1);
		height2 = (EditText) findViewById(R.id.heightentry2);
		heightUnitSpinner = (Spinner) findViewById(R.id.heightunit);
		weight1 = (EditText) findViewById(R.id.weightentry1);
		weight2 = (EditText) findViewById(R.id.weightentry2);
		weightUnitSpinner = (Spinner) findViewById(R.id.weightunit);
		age = (EditText) findViewById(R.id.ageentry);
		neckSize = (EditText) findViewById(R.id.necksizeentry);
		neckSizeUnitSpinner = (Spinner) findViewById(R.id.necksizeunit);
		stopBang8 = (RadioGroup) findViewById(R.id.stopbang8); // gender
		ethnicitySpinner = (Spinner) findViewById(R.id.stopbang9);

		heightUnitSpinner.setOnItemSelectedListener(new HeightSpinnerListener());
		weightUnitSpinner.setOnItemSelectedListener(new WeightSpinnerListener());
		neckSizeUnitSpinner.setOnItemSelectedListener(new NeckSizeSpinnerListener());

		Button next = (Button) findViewById(R.id.stopbangSubmit);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				// Check if all questions were answered, tell user if they
				// weren't.
				boolean question1Answered = stopBang1.getCheckedRadioButtonId() > -1;
				boolean question2Answered = stopBang2.getCheckedRadioButtonId() > -1;
				boolean question3Answered = stopBang3.getCheckedRadioButtonId() > -1;
				boolean question4Answered = stopBang4.getCheckedRadioButtonId() > -1;
				boolean question5Answered = !(height1.getText().toString().equals("") || weight1.getText().toString().equals(""));
				boolean question6Answered = !(age.getText().toString().equals(""));
				boolean question7Answered = !(neckSize.getText().toString().equals(""));
				boolean question8Answered = stopBang8.getCheckedRadioButtonId() > -1;
				boolean question9Answered = true; // true by default since it's a spinner with "White" selected.
				boolean allAnswered = true;
				
				// Go through each question - if it wasn't answered, flag allAnswered as false, and change the relevant
				// question to red font.
				int lightRed = getResources().getColor(R.color.darkred);
				if (!question1Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang1question)).setTextColor(lightRed);
				}
				if (!question2Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang2question)).setTextColor(lightRed);
				}
				if (!question3Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang3question)).setTextColor(lightRed);
				}
				if (!question4Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang4question)).setTextColor(lightRed);
				}
				if (!question5Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang5question)).setTextColor(lightRed);
				}
				if (!question6Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang6question)).setTextColor(lightRed);
				}
				if (!question7Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang7question)).setTextColor(lightRed);
				}
				if (!question8Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang8question)).setTextColor(lightRed);
				}
				if (!question9Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang9question)).setTextColor(lightRed);
				}
					
				if (!allAnswered) {
					Toast.makeText(getApplicationContext(), "Please answer all the questions. Unanswered questions will appear red.", Toast.LENGTH_LONG).show();
					return;
				}

				// Calculate score.
				int score = 0;

				// Responses are in order of questions, except the last two are height and weight respectively.
				String[] responses = new String[12];

				// Going to do the non-radio button responses first as these answers involve some calculation.
				// Calculate BMI.
				float heightM;
				float weightKg;
				switch (selectedHeightUnit) {
				case Metres:
					heightM = floatFromEditText(height1);
					responses[9] = Float.toString(heightM);
					break;
				case FeetInches:
					heightM = (0.3048f * floatFromEditText(height1)) + (0.0254f * floatFromEditText(height2));
					responses[9] = Float.toString(heightM);
					break;
				default:
					String msg = "Wrong enum type for HeightUnit";
					Log.d("StopBang", msg);
					throw new IllegalArgumentException(msg);
				}
				switch (selectedWeightUnit) {
				case Kilograms:
					weightKg = floatFromEditText(weight1);
					responses[10] = Float.toString(weightKg);
					break;
				case StonePounds:
					weightKg = (6.350f * floatFromEditText(weight1)) + (0.454f * floatFromEditText(weight2));
					responses[10] = Float.toString(weightKg);
					break;
				case Pounds:
					weightKg = 0.454f * floatFromEditText(weight1);
					responses[10] = Float.toString(weightKg); // bmi
					break;
				default:
					String msg = "Wrong enum type for WeightUnit";
					Log.d("StopBang", msg);
					throw new IllegalArgumentException(msg);
				}
				float bmi = weightKg / (heightM * heightM);
				
				// Get age.
				int ageInt = (int) floatFromEditText(age);
				
				// Get neck size.
				float neckSizeInches;
				switch (selectedNeckSizeUnit) {
				case Inches:
					neckSizeInches = floatFromEditText(neckSize);
					break;
				case Centimetres:
					neckSizeInches = 0.394f*floatFromEditText(neckSize);
					break;
				default:
					String msg = "Wrong enum type for NeckSizeUnit";
					Log.d("StopBang", msg);
					throw new IllegalArgumentException(msg);
				}

				// Add relevant score for each answer. Radio button IDs are the
				// order on which they appear in the XML layout file, so if they
				// are changed the 'if' statements in the scoring below must be
				// adjusted.
				if (stopBang1.getCheckedRadioButtonId() == R.id.stopbang1ans1) { // snore loudly?
					score++;
					responses[0] = "1"; // yes
				} else {
					responses[0] = "0"; // no
				}

				if (stopBang2.getCheckedRadioButtonId() == R.id.stopbang2ans1) { // tired in the daytime?
					score++;
					responses[1] = "1";
				} else {
					responses[1] = "0";
				}

				if (stopBang3.getCheckedRadioButtonId() == R.id.stopbang3ans1) { // observed not breathing during sleep
					score ++;
					responses[2] = "1";
				} else {
					responses[2] = "0";
				}

				if (stopBang4.getCheckedRadioButtonId() == R.id.stopbang4ans1) { // treated for high blood pressure?
					score++;
					responses[3] = "1";
				} else {
					responses[3] = "0";
				}

				if (bmi > 35) {
					score++;
				}
				responses[4] = String.valueOf(bmi);

				if (ageInt > 50) {
					score++;
				}
				responses[5] = String.valueOf(ageInt);

				if (neckSizeInches >= 16) {
					score++;
				}
				responses[6] = String.valueOf(neckSizeInches);

				if (stopBang8.getCheckedRadioButtonId() == R.id.stopbang8ans1) { // Gender
					score++;
					responses[7] = "1";
				} else {
					responses[7] = "0";
				}

				responses[8] = ethnicitySpinner.getSelectedItem().toString();
				responses[11] = String.valueOf(score);

				// Record answers.
				try {
					// Create CSV file. Don't change this without
					// updating file reads elsewhere in the app, otherwise it's
					// going to be crash central.
					File appDir = new File(Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name) + "/");
					
					// Create a directory if there is no SD card.
					if (!appDir.exists()) {
						appDir.mkdirs();
					}
					
					File outputFile = new File(appDir, Constants.FILENAME_QUESTIONNAIRE);
					FileWriter fileWriter = new FileWriter(outputFile);
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					for (int i = 0; i < responses.length; i++) {
						if (!responses[i].isEmpty()) {
							bufferedWriter.write(responses[i] + "\n");
						} else {
							bufferedWriter.write("NaN \n");
						}
					}

					bufferedWriter.flush();
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException e) {
					Log.e("StopBang", "IOException when writing to file");
				}

				Intent intent = new Intent(StopBangQuestionnaire.this, StopBangResults.class);
				intent.putExtra(Constants.EXTRA_SCORE, score);
				startActivity(intent);
				overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);
			}
		});
	}

	private class HeightSpinnerListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			String unit = parent.getItemAtPosition(pos).toString();

			if (unit.equals("Metres")) {
				// Metres selected.
				selectedHeightUnit = HeightUnit.Metres;
				height2.setVisibility(View.INVISIBLE);
				return;
			}

			if (unit.equals("Feet/Inches")) {
				// Feet and Inches selected.
				selectedHeightUnit = HeightUnit.FeetInches;
				height2.setVisibility(View.VISIBLE);
				return;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	private class WeightSpinnerListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			String unit = parent.getItemAtPosition(pos).toString();

			if (unit.equals("Kilograms")) {
				// Kilograms selected.
				selectedWeightUnit = WeightUnit.Kilograms;
				weight2.setVisibility(View.INVISIBLE);
				return;
			}

			if (unit.equals("Stone/Pounds")) {
				// Stone and pounds selected.
				selectedWeightUnit = WeightUnit.StonePounds;
				weight2.setVisibility(View.VISIBLE);
				return;
			}

			if (unit.equals("Pounds")) {
				// Stone and pounds selected.
				selectedWeightUnit = WeightUnit.Pounds;
				weight2.setVisibility(View.INVISIBLE);
				return;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}
	
	private class NeckSizeSpinnerListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			String unit = parent.getItemAtPosition(pos).toString();

			if (unit.equals("Inches")) {
				// Inches selected.
				selectedNeckSizeUnit = NeckSizeUnit.Inches;
				return;
			}

			if (unit.equals("Centimetres")) {
				// Centimetres selected.
				selectedNeckSizeUnit = NeckSizeUnit.Centimetres;
				return;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	private float floatFromEditText(EditText view) {
		String text = view.getText().toString();
		if (text.equals("")) {
			return 0;
		}
		return Float.parseFloat(text);
	}
}