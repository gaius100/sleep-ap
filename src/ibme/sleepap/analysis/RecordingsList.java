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
import ibme.sleepap.MainMenu;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordingsList extends SleepApActivity implements OnClickListener {

	List<File> folderList;
	TextView noFilesFound;
	File fQuestionnaire = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recordings_list);
		LinearLayout mainLayout = (LinearLayout) findViewById(R.id.results_fileListLL);

		File appDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + getString(R.string.app_name));
		if (appDir.isDirectory()) {

			File[] fileList = appDir.listFiles();
			folderList = new ArrayList<File>();

			if (fileList != null && fileList.length > 0) {
				noFilesFound = (TextView) findViewById(R.id.noFilesFound);
				noFilesFound.setVisibility(View.GONE);

				for (int i = 0; i < fileList.length; i++) {
					if (fileList[i].isDirectory()) {
						if (!fileList[i].getName().equals(Constants.FILENAME_QUESTIONNAIRE_DIRECTORY)
								&& !fileList[i].getName().equals(Constants.FILENAME_LOG_DIRECTORY)) {
							// Add folder at the start of the list.
							folderList.add(0, fileList[i]);
						} else {
							// fQuestionnaire = fileList[i]; // commented out-
							// to be implemented in later version
						}
					}
				}
				Collections.sort(folderList);
				if (fQuestionnaire != null)
					folderList.add(0, fQuestionnaire); // not functional in this
														// version
				for (int i = 0; i < folderList.size(); i++) {
					Button newButton = new Button(this);
					newButton.setTextColor(Color.DKGRAY);
					try {
						if ((i != 0) || (fQuestionnaire == null)) {
							newButton.setText(extractDateInfo(folderList.get(i).getName()));
						} else {
							newButton.setText("Questionnaire Only"); // not
																		// functional
																		// in
																		// this
																		// version
						}
						newButton.setTextSize(20);
						newButton.setGravity(Gravity.CENTER);
						newButton.setTextColor(Color.WHITE);
						newButton.setId(i);
						LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Utils.dipToPixels(50,
								this));
						param.topMargin = Utils.dipToPixels(10, this);
						newButton.setOnClickListener(RecordingsList.this);
						mainLayout.addView(newButton, param);
					} catch (Exception e) {
						e.printStackTrace();
						noFilesFound.setText("File System Corrupted. Please delete extraneous folder " + folderList.get(i).getName()
								+ ", from /SleepAp/ Folder");
						noFilesFound.setVisibility(View.VISIBLE);// should
																	// include
																	// an option
																	// to delete
																	// the saved
																	// folders
					}
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		int index = v.getId();
		if ((index != 0) || (fQuestionnaire == null)) {
			noFilesFound.setVisibility(View.GONE);
			File recordingDir = folderList.get(index);
			Intent intent = new Intent(this, ChooseData.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intent.putExtra(Constants.EXTRA_RECORDING_DIRECTORY, recordingDir);
			startActivity(intent);
			overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);
		} else { // TODO
			noFilesFound.setText("Not Functional Yet");
			noFilesFound.setVisibility(View.VISIBLE);
		}
	}

	public String extractDateInfo(String fileName) {
		String dateVal = fileName.substring(4, 6) + "/" + fileName.substring(6, 8) + "/" + fileName.substring(2, 4);
		String timeVal = fileName.substring(8, 10) + ":" + fileName.substring(10, 12) + ":" + fileName.substring(12, 14);
		return timeVal + " on " + dateVal;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(RecordingsList.this, MainMenu.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Constants.EXTRA_HIDE_LICENCE, true);
		startActivity(intent);
	}
}