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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class Feedback extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);

		final RadioGroup feedback1 = (RadioGroup) findViewById(R.id.feedback1);
		final RadioGroup feedback2 = (RadioGroup) findViewById(R.id.feedback2);
		final RadioGroup feedback3 = (RadioGroup) findViewById(R.id.feedback3);
		final RadioGroup feedback4 = (RadioGroup) findViewById(R.id.feedback4);
		final EditText feedback5 = (EditText) findViewById(R.id.feedback5response);

		Button submitButton = (Button) findViewById(R.id.feedbackSubmit);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String[] responses = new String[5];

				switch (feedback1.getCheckedRadioButtonId()) {
				case R.id.feedback1ans1:
					responses[0] = "1";
					break;
				case R.id.feedback1ans2:
					responses[0] = "2";
					break;
				case R.id.feedback1ans3:
					responses[0] = "3";
					break;
				case R.id.feedback1ans4:
					responses[0] = "4";
					break;
				case R.id.feedback1ans5:
					responses[0] = "5";
					break;
				default:
					responses[0] = "-1";
				}

				switch (feedback2.getCheckedRadioButtonId()) {
				case R.id.feedback2ans1:
					responses[1] = "1";
					break;
				case R.id.feedback2ans2:
					responses[1] = "2";
					break;
				case R.id.feedback2ans3:
					responses[1] = "3";
					break;
				case R.id.feedback2ans4:
					responses[1] = "4";
					break;
				case R.id.feedback2ans5:
					responses[1] = "5";
					break;
				case R.id.feedback2ans6:
					responses[1] = "6";
					break;
				default:
					responses[1] = "-1";
				}

				switch (feedback3.getCheckedRadioButtonId()) {
				case R.id.feedback3ans1:
					responses[2] = "1";
					break;
				case R.id.feedback3ans2:
					responses[2] = "2";
					break;
				case R.id.feedback3ans3:
					responses[2] = "3";
					break;
				case R.id.feedback3ans4:
					responses[2] = "4";
					break;
				default:
					responses[2] = "-1";
				}

				switch (feedback4.getCheckedRadioButtonId()) {
				case R.id.feedback4ans1:
					responses[3] = "1";
					break;
				case R.id.feedback4ans2:
					responses[3] = "2";
					break;
				case R.id.feedback4ans3:
					responses[3] = "3";
					break;
				case R.id.feedback4ans4:
					responses[3] = "4";
					break;
				case R.id.feedback4ans5:
					responses[3] = "5";
					break;
				default:
					responses[3] = "-1";
				}

				responses[4] = TextUtils.htmlEncode(feedback5.getText().toString());

				// Record answers in CSV file.
				try {
					File feedbackDir = new File(Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name) + "/"
							+ Constants.FILENAME_FEEDBACK_DIRECTORY + "/");

					// Create the directory if necessary.
					if (!feedbackDir.exists()) {
						feedbackDir.mkdirs();
					}

					File outputFile = new File(feedbackDir, DateFormat.format(Constants.PARAM_DATE_FORMAT, System.currentTimeMillis()).toString() + ".dat");
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
					Log.e(Constants.CODE_APP_TAG, "IOException when writing feedback to file");
				}

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Feedback.this);
				dialogBuilder.setTitle(getString(R.string.feedbackAlertTitle)).setMessage(getString(R.string.feedbackAlertMessage))
						.setNegativeButton(getString(R.string.mainMenuButtonText), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Feedback.this, MainMenu.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
						}).setCancelable(false);
				dialogBuilder.create().show();
			}
		});
	}
}
