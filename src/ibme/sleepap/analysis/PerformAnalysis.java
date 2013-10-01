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
import ibme.sleepap.CustomExceptionHandler;
import ibme.sleepap.MainMenu;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.Utils;
import ibme.sleepap.history.DatabaseHelper;
import ibme.sleepap.history.HistoryTable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PerformAnalysis extends SleepApActivity {

	private SharedPreferences sharedPreferences;
	private TextView questionnaireScore, svmScore, odiScore;
	//private ServerAccess serverAccess;
	//private Handler cloudStatusHandler;
	private File actigraphyFile, audioFile, demographicsFile, spo2File, recordingDir;
	private List<Double> svmAudioValues = new ArrayList<Double>();
	private List<Double> svmActigraphyValues = new ArrayList<Double>();
	private List<Double> svmDemographicsValues = new ArrayList<Double>();
	private List<Double> svmOdiValues = new ArrayList<Double>();
	private boolean[] areTasksNecessary = { true, true, true, true, true };
	private boolean[] areTasksCompleted = { false, false, false, false, false };
	private boolean[] areTasksSuccessful = { false, false, false, false, false };
	private ProgressDialog progressDialog;
	private StringBuilder progressDialogMessage = new StringBuilder();
	private ContentValues databaseEntry = new ContentValues();
	private SQLiteDatabase datasource;
	private Map<String, Object> cloudEntry = new LinkedHashMap<String, Object>();
	private Date recordingDate;
	//private Button cloudButton;
	private CalculateAudioMse calculateAudioMse;
	private CalculateActigraphyMse calculateActigraphyMse;
	private AnalyseDemographics analyseDemographics;
	private AnalyseSpo2 analyseSpo2;
	private RunMachineLearning runMachineLearning;
	private boolean runMachineLearningCalled = false;

	private static final int[] mseScales = { 1, 2, 4, 8, 16, 32, 65, 130, 180 };
	private static final double[] normalizingMean = { 1.070235, 0.866186, 0.706777, 0.699142, 0.629209, 0.525957, 
		0.473172, 0.483983, 0.507266, 0.293280, 0.239864, 0.226303, 0.223415, 0.248910, 0.295175, 0.345406, 
		0.361574, 0.365361, 0.667183, 48.921053, 16.715195, 68.235911, 221.274951, 33.480832, 21.975125 };
	private static final double[] normalizingStd = {0.450591, 0.383389, 0.298440, 0.309937, 0.292578, 
		0.225219, 0.192946, 0.197889, 0.208613, 0.114931, 0.107838, 0.117124, 0.102084, 0.095489, 
		0.100784, 0.118337, 0.143646, 0.156476, 0.471587, 13.625727, 1.857124, 3.782977, 62.609251, 
		9.437609, 28.630741};

	/*private Runnable cloudStatusUpdater = new Runnable() {
		@Override
		public void run() {
			serverAccess.UpdateStatus();
		}
	};*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.perform_analysis);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		/** Error logging. */
		// Log both handled and unhandled issues.
		if (sharedPreferences.getBoolean(Constants.PREF_WRITE_LOG, Constants.DEFAULT_WRITE_LOG)) {
			String bugDirPath = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name) + "/"
					+ Constants.FILENAME_LOG_DIRECTORY;
			File bugDir = new File(bugDirPath);
			if (!bugDir.exists()) {
				bugDir.mkdirs();
			}
			String handledFileName = bugDirPath + "/logcat" + System.currentTimeMillis() + ".trace";
			String unhandledFileName = bugDirPath + "/unhandled" + System.currentTimeMillis() + ".trace";
			// Log any warning or higher, and write it to handledFileName.
			String[] cmd = new String[] { "logcat", "-f", handledFileName, "*:W" };
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e1) {
				Log.e(Constants.CODE_APP_TAG, "Error creating bug files", e1);
			}
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(unhandledFileName));
		}

		/** Get database. */
		datasource = new DatabaseHelper(this).getWritableDatabase();

		/** Get files. */
		Intent sendingIntent = getIntent();
		actigraphyFile = (File) sendingIntent.getSerializableExtra(Constants.EXTRA_ACTIGRAPHY_FILE);
		audioFile = (File) sendingIntent.getSerializableExtra(Constants.EXTRA_AUDIO_FILE);
		demographicsFile = (File) sendingIntent.getSerializableExtra(Constants.EXTRA_DEMOGRAPHICS_FILE);
		// ppgFile = (File) sendingIntent.getSerializableExtra(Constants.EXTRA_PPG_FILE);
		spo2File = (File) sendingIntent.getSerializableExtra(Constants.EXTRA_SPO2_FILE);
		recordingDir = (File) sendingIntent.getSerializableExtra(Constants.EXTRA_RECORDING_DIRECTORY);
		String recordingDateString = recordingDir.getName();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
		try {
			recordingDate = formatter.parse(recordingDateString);
			cloudEntry.put("start_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(recordingDate));
		} catch (ParseException e) {
			recordingDate = null;
			cloudEntry.put("start_date", null);
		}

		/** Set up score TextViews. */
		questionnaireScore = (TextView) findViewById(R.id.stopBang);
		svmScore = (TextView) findViewById(R.id.svmOutput);
		odiScore = (TextView) findViewById(R.id.odi);
		//cloudScore = (TextView) findViewById(R.id.cloudOutput);

		/** Set up cloud connection. */
		/*cloudButton = ((Button) findViewById(R.id.cloudButton));
		cloudButton.setEnabled(false);
		cloudButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				serverAccess.sendData(cloudEntry);
			}
		});
		cloudStatusHandler = new Handler();
		serverAccess = new ServerAccess(this, cloudScore, cloudStatusHandler, cloudStatusUpdater);
		serverAccess.setDB(datasource, recordingDateString);*/

		/** Set up help buttons. */
		final int[] helpButtonIds = { R.id.odiHelp, R.id.stopBangHelp, R.id.svmHelp, R.id.cloudHelp };
		final int[] helpTitleIds = { R.string.odiHelpTitle, R.string.stopBangHelpTitle, R.string.svmHelpTitle, R.string.cloudHelpTitle };
		final int[] helpMessageIds = { R.string.odiHelpMessage, R.string.stopBangHelpMessage, R.string.svmHelpMessage, R.string.cloudHelpMessage };
		for (int i = 0; i < helpButtonIds.length; i++) {
			final int idx = i;
			((ImageButton) findViewById(helpButtonIds[idx])).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PerformAnalysis.this);
					dialogBuilder.setTitle(getString(helpTitleIds[idx])).setMessage(getString(helpMessageIds[idx]))
							.setPositiveButton(getString(R.string.ok), null);
					dialogBuilder.create().show();
				}
			});
		}
		
		
		//Query database to see if this recording has been analysed before. If it has we wipe the results and reanalyse - might want to change this in future. 
		Cursor cursor = datasource.query(HistoryTable.TABLE_NAME, HistoryTable.ALL_COLUMNS, HistoryTable.COLUMN_START_DATE + " = " + recordingDateString, null,
				null, null, null);
		if (cursor.getCount() > 0) {
			datasource.delete(HistoryTable.TABLE_NAME, HistoryTable.COLUMN_START_DATE + " = " + recordingDateString, null);
		}
		cursor.close();
		
		/** Set up detailed results. */
		final ImageView expandDetailedResultsArrow = (ImageView) findViewById(R.id.detailedResultsExpand);
		final LinearLayout detailedResults = (LinearLayout) findViewById(R.id.detailedResults);
		((TextView) findViewById(R.id.detailedResultsTitle)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (detailedResults.getVisibility() == View.GONE) {
					detailedResults.setVisibility(View.VISIBLE);
					expandDetailedResultsArrow.setImageResource(R.drawable.navigationcollapse);
				} else {
					detailedResults.setVisibility(View.GONE);
					expandDetailedResultsArrow.setImageResource(R.drawable.navigationexpand);
				}
			}
		});

		/** Progress dialog display. */
		progressDialogMessage.append(getString(R.string.loadingMessage));
		progressDialog = ProgressDialog.show(this, getString(R.string.loading), progressDialogMessage.toString());
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				progressDialog.dismiss();
				onBackPressed();
			}
		});

		/** Start analysis. */
		if (audioFile != null) {
			calculateAudioMse = new CalculateAudioMse();
			calculateAudioMse.execute();
		} else {
			areTasksNecessary[0] = false;
		}
		if (actigraphyFile != null) {
			calculateActigraphyMse = new CalculateActigraphyMse();
			calculateActigraphyMse.execute();
		} else {
			areTasksNecessary[1] = false;
		}
		if (demographicsFile != null) {
			analyseDemographics = new AnalyseDemographics();
			analyseDemographics.execute();
		} else {
			areTasksNecessary[2] = false;
		}
		// PPG not yet analysed.
		areTasksCompleted[2] = true;
		areTasksSuccessful[2] = true;
		areTasksNecessary[3] = false;
		if (spo2File != null) {
			analyseSpo2 = new AnalyseSpo2();
			analyseSpo2.execute();
		} else {
			areTasksNecessary[4] = false;
		}
	}

	@Override
	public void onBackPressed() {
		if (calculateAudioMse != null && calculateAudioMse.isRunning()) {
			calculateAudioMse.cancel(true);
		}
		if (calculateActigraphyMse != null && calculateActigraphyMse.isRunning()) {
			calculateActigraphyMse.cancel(true);
		}
		if (analyseDemographics != null && analyseDemographics.isRunning()) {
			analyseDemographics.cancel(true);
		}
		if (analyseSpo2 != null && analyseSpo2.isRunning()) {
			analyseSpo2.cancel(true);
		}
		if (runMachineLearning != null && runMachineLearning.isRunning()) {
			runMachineLearning.cancel(true);
		}
		Intent intent = new Intent(PerformAnalysis.this, MainMenu.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Constants.EXTRA_HIDE_LICENCE, true);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		datasource.close();
		super.onDestroy();
	}

	private class CalculateAudioMse extends AsyncTask<Void, Void, Void> {
		private boolean _isRunning;

		@Override
		protected Void doInBackground(Void... params) {
			_isRunning = true;
			try {
				List<double[]> scaledData = new ArrayList<double[]>();
				double[] mseValues = new double[mseScales.length];
				long[] loadingTime = new long[mseScales.length];
				double[] data = doubleListToDoubleArray(Utils.parseCsvFile(audioFile, 0));
				double sd = MultiScaleEntropy.standardDeviation(data);
				long startTime;
				for (int i = mseScales.length - 1; i >= 0; i--) {
					startTime = System.currentTimeMillis();
					double[] currentScaledData = coarseGrain(data, mseScales[i]);
					scaledData.add(currentScaledData);
					mseValues[i] = MultiScaleEntropy.SampleEntropy(currentScaledData, .25, sd, mseScales[i], 2);
					loadingTime[i] = System.currentTimeMillis() - startTime;
				}
				for (int i = 0; i < mseValues.length; i++) {
					if (Double.isInfinite(mseValues[i])) {
						mseValues[i] = Double.NaN;
					}
				}
				FileWriter fileWriter = new FileWriter(new File(recordingDir, Constants.FILENAME_AUDIO_MSE), false);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(MultiScaleEntropy.getCsvFormattedOutput(mseScales, mseValues, loadingTime));
				bufferedWriter.flush();
				bufferedWriter.close();
				fileWriter.close();
				for (int i = 0; i < mseValues.length; i++) {
					svmAudioValues.add(((mseValues[i] - normalizingMean[i + 10]) / normalizingStd[i + 10]));
				}
				cloudEntry.put("audio", mseValues);
				publishProgress();
				areTasksSuccessful[0] = true;
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG, "IOException calculating audio MSE data and writing to file", e);
			}
			areTasksCompleted[0] = true;
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			progressDialogMessage.append(getString(R.string.audioAnalysisDone));
			progressDialog.setMessage(progressDialogMessage.toString());
		}

		@Override
		protected void onPostExecute(Void result) {
			_isRunning = false;
			if (areAllTasksCompleted() && !runMachineLearningCalled) {
				runMachineLearning = new RunMachineLearning();
				runMachineLearning.execute();
				runMachineLearningCalled = true;
			}
		}

		protected boolean isRunning() {
			return _isRunning;
		}
	}

	private class CalculateActigraphyMse extends AsyncTask<Void, Void, Void> {
		private boolean _isRunning = false;

		@Override
		protected Void doInBackground(Void... params) {
			_isRunning = true;
			try {
				List<double[]> scaledData = new ArrayList<double[]>();
				double[] mseValues = new double[mseScales.length];
				long[] loadingTime = new long[mseScales.length];
				double[] data = doubleListToDoubleArray(Utils.parseCsvFile(actigraphyFile, 0));
				double sd = MultiScaleEntropy.standardDeviation(data);
				long startTime;
				for (int i = mseScales.length - 1; i >= 0; i--) {
					startTime = System.currentTimeMillis();
					double[] currentScaledData = coarseGrain(data, mseScales[i]);
					scaledData.add(currentScaledData);
					mseValues[i] = MultiScaleEntropy.SampleEntropy(currentScaledData, .25, sd, mseScales[i], 2);
					loadingTime[i] = System.currentTimeMillis() - startTime;
				}
				for (int i = 0; i < mseValues.length; i++) {
					if (Double.isInfinite(mseValues[i])) {
						mseValues[i] = Double.NaN;
					}
				}
				FileWriter fileWriter = new FileWriter(new File(recordingDir, Constants.FILENAME_ACCELERATION_MSE), false);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(MultiScaleEntropy.getCsvFormattedOutput(mseScales, mseValues, loadingTime));
				bufferedWriter.flush();
				bufferedWriter.close();
				fileWriter.close();
				for (int i = 0; i < mseValues.length; i++) {
					svmActigraphyValues.add(((mseValues[i] - normalizingMean[i]) / normalizingStd[i]));
				}
				cloudEntry.put("actigraphy", mseValues);
				publishProgress();
				areTasksSuccessful[1] = true;
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG, "IOException calculating actigraphy MSE data and writing to file", e);
			}
			areTasksCompleted[1] = true;
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			progressDialogMessage.append(getString(R.string.actigraphyAnalysisDone));
			progressDialog.setMessage(progressDialogMessage.toString());
		}

		@Override
		protected void onPostExecute(Void result) {
			_isRunning = false;
			if (areAllTasksCompleted() && !runMachineLearningCalled) {
				runMachineLearning = new RunMachineLearning();
				runMachineLearning.execute();
				runMachineLearningCalled = true;
			}
		}

		protected boolean isRunning() {
			return _isRunning;
		}
	}

	private class AnalyseDemographics extends AsyncTask<Void, Void, Void> {
		private int _stopBangScore;
		private boolean _isRunning;

		@Override
		protected Void doInBackground(Void... params) {
			_isRunning = true;
			DemographicsContainer demographics = new DemographicsContainer(demographicsFile);
			_stopBangScore = demographics.getScore();
			String gender = demographics.getGender();
			int genderSwitch;
			if (gender.equalsIgnoreCase("male")) {
				genderSwitch = 1;
			} else {
				genderSwitch = 0;
			}
			// Gender, age, necksize, height, weight, BMI.
			svmDemographicsValues.add((genderSwitch - normalizingMean[18]) / normalizingStd[18]);
			svmDemographicsValues.add((demographics.getAge() - normalizingMean[19]) / normalizingStd[19]);
			svmDemographicsValues.add((demographics.getNeckSize() - normalizingMean[20]) / normalizingStd[20]);
			svmDemographicsValues.add((demographics.getHeight() - normalizingMean[21]) / normalizingStd[21]);
			svmDemographicsValues.add((demographics.getWeight() - normalizingMean[22]) / normalizingStd[22]);
			svmDemographicsValues.add((demographics.getBmi() - normalizingMean[23]) / normalizingStd[23]);
			areTasksSuccessful[2] = true;
			databaseEntry.put(HistoryTable.COLUMN_QUESTIONNAIRE, String.valueOf(demographics.getScore()));
			areTasksCompleted[2] = true;
			cloudEntry.put("gender", demographics.getGender());
			cloudEntry.put("age", demographics.getAge());
			cloudEntry.put("neck_size", demographics.getNeckSize());
			cloudEntry.put("height", demographics.getHeight());
			cloudEntry.put("weight", demographics.getWeight());
			cloudEntry.put("bmi", demographics.getBmi());
			publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			questionnaireScore.setText(getString(R.string.stopBangDefaultScore) + " " + String.valueOf(_stopBangScore));
			findViewById(R.id.stopBangContainer).setVisibility(View.VISIBLE);
			progressDialogMessage.append(getString(R.string.demographicsAnalysisDone));
			progressDialog.setMessage(progressDialogMessage.toString());
		}

		@Override
		protected void onPostExecute(Void result) {
			_isRunning = false;
			if (areAllTasksCompleted() && !runMachineLearningCalled) {
				runMachineLearning = new RunMachineLearning();
				runMachineLearning.execute();
				runMachineLearningCalled = true;
			}
		}

		protected boolean isRunning() {
			return _isRunning;
		}
	}

	private class AnalyseSpo2 extends AsyncTask<Void, Void, Void> {
		private float _odi;
		private boolean _isRunning;
		/** ODI calculated as described in
		 *  Chung F, et al. Oxygen Desaturation Index from Nocturnal
		 *	Oximetry: A Sensitive and Specific Tool to Detect 
		 *  Sleep-Disordered Breathing in Surgical Patients. Anesthesia and
		 *  analgesia May 2012; 114(5): 993- 1000.
		
		 *  This is algorithm is being optimized currently.
		**/
		@Override
		protected Void doInBackground(Void... params) {
			_isRunning = true;
			try {
				List<Double> Spo2Data = Utils.parseCsvFile(spo2File, 1);
				_odi = Utils.odiCalculate(Spo2Data,
						Integer.parseInt(sharedPreferences.getString(Constants.PREF_ODI_THRESHOLD, Constants.DEFAULT_ODI_THRESHOLD)));

				svmOdiValues.add(((_odi - normalizingMean[24]) / normalizingStd[24]));
				areTasksSuccessful[4] = true;
				databaseEntry.put(HistoryTable.COLUMN_ODI, String.format("%.1f", _odi));
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG, "IOException parsing spo2 data", e);
			}
			areTasksCompleted[4] = true;
			cloudEntry.put("odi", _odi);
			publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			if (!Double.isNaN(_odi)) {
				odiScore.setText(getString(R.string.odiDefaultScore) + " " + String.format("%.1f", _odi));
			} else {
				Log.w(Constants.CODE_APP_TAG, "spo2 file too short to calculate ODI.");
				odiScore.setText(getString(R.string.odiDefaultScore) + "-");
			}
			findViewById(R.id.odiContainer).setVisibility(View.VISIBLE);
			progressDialogMessage.append(getString(R.string.odiAnalysisDone));
			progressDialog.setMessage(progressDialogMessage.toString());
		}

		@Override
		protected void onPostExecute(Void result) {
			_isRunning = false;
			if (areAllTasksCompleted() && !runMachineLearningCalled) {
				runMachineLearning = new RunMachineLearning();
				runMachineLearning.execute();
				runMachineLearningCalled = true;
			}
		}

		protected boolean isRunning() {
			return _isRunning;
		}
	}

	private class RunMachineLearning extends AsyncTask<Void, Void, Void> {
		private double _svmOutput;
		private boolean _isRunning;

		@Override
		protected Void doInBackground(Void... params) {
			_isRunning = true;
			int svmResourceId = chooseSVM(areTasksSuccessful);
			if (svmResourceId == 0) {
				Log.e(Constants.CODE_APP_TAG, "Can't run SVM - invalid resource selected");
				return null;
			}

			// Compile SVM features.
			List<Double> svmFeatures = new ArrayList<Double>();
			if (areTasksSuccessful[0])
				svmFeatures.addAll(svmAudioValues);
			if (areTasksSuccessful[1])
				svmFeatures.addAll(svmActigraphyValues);
			if (areTasksSuccessful[2])
				svmFeatures.addAll(svmDemographicsValues);
			if (areTasksSuccessful[4])
				svmFeatures.addAll(svmOdiValues);

			/** For Testing Purposes **/

			
			/** double [] svmInputsTestPositiveArray= {0.7424, 0.2986,0.7830,0.3153,
					 -0.1779,-0.3235,-0.2686,-0.0693,-0.1777,-1.1720,-0.9380,
					 -0.9739,-0.8235,-0.8158,-1.0603,-1.4836,-0.9535,-0.3531,
					 -1.4396, 1.1088, -0.3718,-1.9334, 0.7674, 1.8545, 1.1289}; // should return a 1 and probability of [0.9583 0.0417] 
			double[] svmInputsTestNegativeArray= {1.5647, 1.0554, 0.8604, 0.6650, 
					0.5185, 0.5481, 0.2661, 0.3582, 0.0754, -0.6229, -0.5877,
					-0.6681, -0.6625, -0.7347, -0.6117, -0.2119, 0.1753, 0.0813,
					0.6937, 3.1971, 0.4523, -0.3419, -0.4869, -0.3753, -0.6879};
			
			 List<Double> svmTestEx = new ArrayList<Double>(); 
			 //for(int i = 0; i<svmInputsTestPositiveArray.length; i++){ // uncomment this for Positive result 
				// svmTestEx.add(svmInputsTestPositiveArray[i]); 
				 //}
			 for(int i = 0; i<svmInputsTestNegativeArray.length; i++){ // uncomment this for Negative result
				svmTestEx.add(svmInputsTestNegativeArray[i]); 
			 	}
			 svmResourceId= R.raw.actauddemoodi_svm;
			// End Test Code
				*/
			svm_model svmModel = new svm_model();
			try {
				InputStream rawRes = getResources().openRawResource(svmResourceId);
				Reader r = new InputStreamReader(rawRes);
				svmModel = svm.svm_load_model(new BufferedReader(r));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			double[] probabilityEstimates = new double[2];
			svm_node[] svmNode = new svm_node[svmFeatures.size()]; // for testing- use svmTestEx instead of svmFeatures

			// For test purposes use svmTestEx instead of svmFeatures in this
			// loop.
			for (int i = 0; i < svmFeatures.size(); i++) {
				svmNode[i] = new svm_node();
				svmNode[i].index = i + 1;
				svmNode[i].value = svmFeatures.get(i);
			}
			svmModel.param.probability = 1; // this may not be necessary
			
			_svmOutput = svm.svm_predict_probability(svmModel, svmNode, probabilityEstimates);
			// probabilityEstimates should give a percentage value of
			// probability for each class/label ([apnea, non-apnea])
			_svmOutput = probabilityEstimates[0]; // probability for sleep apnea
			try {
				FileWriter fileWriter = new FileWriter(new File(recordingDir, Constants.FILENAME_SVM_OUTPUT), false);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write("" + _svmOutput);
				bufferedWriter.flush();
				bufferedWriter.close();
				fileWriter.close();
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG, "IOException writing SVM results to file", e);
			}

			// Update database.

			String displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(recordingDate);
			databaseEntry.put(HistoryTable.COLUMN_START_DATE, recordingDir.getName());
			databaseEntry.put(HistoryTable.COLUMN_START_DATE_DISPLAY, displayFormat);
			// The below rounds the SVM score to 2 s.f. before adding it to the database.
			databaseEntry.put(HistoryTable.COLUMN_SVM_RESULT, String.format("%.2f", (((float) Math.round(_svmOutput*100))/100)));
			databaseEntry.put(HistoryTable.COLUMN_CLOUD_RESULT, "-");
			if (databaseEntry.getAsString(HistoryTable.COLUMN_ODI) == null) {
				databaseEntry.put(HistoryTable.COLUMN_ODI, "-");
			}
			if (datasource != null && datasource.isOpen()) {
				datasource.insert(HistoryTable.TABLE_NAME, null, databaseEntry);
				Log.w(Constants.CODE_APP_TAG, "Analysis saved to database.");
			}

			publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// Update UI
			progressDialog.dismiss();
			TextView overallRisk = (TextView) findViewById(R.id.overallRisk);
			svmScore.setText(getString(R.string.svmDefaultScore) + " " + String.format("%d%%", Math.round(_svmOutput*100)));
			findViewById(R.id.svmOutputContainer).setVisibility(View.VISIBLE);
			if (_svmOutput > 0.5) {
				overallRisk.setText(getString(R.string.atRisk));
				overallRisk.setTextColor(getResources().getColor(R.color.darkred));
			} else {
				overallRisk.setText(getString(R.string.notAtRisk));
				overallRisk.setTextColor(getResources().getColor(R.color.darkgreen));
			}

			// Update thermometer
			ImageView thermometerImage = (ImageView) findViewById(R.id.thermometer);
			// Get mutable bitmap from drawable to copy onto canvas
			Bitmap mutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thermometer).copy(Bitmap.Config.ARGB_8888, true);
			Canvas canvas = new Canvas(mutableBitmap);
			// Draw thermometer onto canvas, then draw 'temperature' onto
			// it.
			canvas.drawBitmap(mutableBitmap, 0, 0, null);
			Thermometer thermometer = new Thermometer(PerformAnalysis.this, canvas);
			thermometer.drawThermometer(_svmOutput);
			thermometerImage.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));

			//cloudButton.setEnabled(true);
		}

		@Override
		protected void onPostExecute(Void result) {
			_isRunning = false;
			super.onPostExecute(result);
		}

		protected boolean isRunning() {
			return _isRunning;
		}
	}

	private double[] doubleListToDoubleArray(List<Double> inputList) {
		int inputLength = inputList.size();
		double[] outputArray = new double[inputLength];
		for (int i = 0; i < inputLength; i++) {
			outputArray[i] = inputList.get(i);
		}
		return outputArray;
	}

	public boolean areAllTasksCompleted() {
		for (int i = 0; i < areTasksCompleted.length; i++) {
			if (areTasksNecessary[i] == true && areTasksCompleted[i] == false) {
				return false;
			}
		}
		return true;
	}

	private double[] coarseGrain(double[] longDoubleArray, int scale) {
		int longLength = longDoubleArray.length;
		int coarseGrainLength = longLength / scale;
		double[] coarseGrainedArray = new double[coarseGrainLength];
		for (int i = 0; i < coarseGrainLength; i++) {
			int startIndex = scale * i;
			double sumOfValues = 0;
			for (int j = startIndex; j < startIndex + scale; j++) {
				sumOfValues += longDoubleArray[j];
			}
			coarseGrainedArray[i] = sumOfValues / scale;
		}
		return coarseGrainedArray;
	}

	private int chooseSVM(boolean[] chosen) {
		// Want to use different SVM depending on what signals were used. The
		// code below maps all possible signal combinations to an SVM.

		// No inputs.
		if (Arrays.equals(chosen, new boolean[] { false, false, false, false, false }))
			return 0;

		// One input.
		if (Arrays.equals(chosen, new boolean[] { true, false, false, false, false }))
			return R.raw.aud_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, false, false, false }))
			return R.raw.act_svm;
		if (Arrays.equals(chosen, new boolean[] { false, false, true, false, false }))
			return R.raw.demo_svm;
		if (Arrays.equals(chosen, new boolean[] { false, false, false, true, false }))
			return 0;
		if (Arrays.equals(chosen, new boolean[] { false, false, false, false, true }))
			return R.raw.odi_svm;

		// Two inputs.
		if (Arrays.equals(chosen, new boolean[] { true, true, false, false, false }))
			return R.raw.actaud_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, true, false, false }))
			return R.raw.auddemo_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, false, true, false }))
			return R.raw.aud_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, false, false, true }))
			return R.raw.audodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, true, false, false }))
			return R.raw.actdemo_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, false, true, false }))
			return R.raw.act_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, false, false, true }))
			return R.raw.actodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, false, true, true, false }))
			return R.raw.demo_svm;
		if (Arrays.equals(chosen, new boolean[] { false, false, true, false, true }))
			return R.raw.demoodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, false, false, true, true }))
			return R.raw.odi_svm;

		// Three inputs.
		if (Arrays.equals(chosen, new boolean[] { true, true, true, false, false }))
			return R.raw.actauddemo_svm;
		if (Arrays.equals(chosen, new boolean[] { true, true, false, true, false }))
			return R.raw.actaud_svm;
		if (Arrays.equals(chosen, new boolean[] { true, true, false, false, true }))
			return R.raw.actaudodi_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, true, true, false }))
			return R.raw.auddemo_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, true, false, true }))
			return R.raw.auddemoodi_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, false, true, true }))
			return R.raw.audodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, true, true, false }))
			return R.raw.actdemo_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, true, false, true }))
			return R.raw.actdemoodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, false, true, true }))
			return R.raw.actodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, false, true, true, true }))
			return R.raw.demoodi_svm;

		// Four inputs.
		if (Arrays.equals(chosen, new boolean[] { true, true, true, true, false }))
			return R.raw.actauddemo_svm;
		if (Arrays.equals(chosen, new boolean[] { true, true, true, false, true }))
			return R.raw.actauddemoodi_svm;
		if (Arrays.equals(chosen, new boolean[] { true, true, false, true, true }))
			return R.raw.actaudodi_svm;
		if (Arrays.equals(chosen, new boolean[] { true, false, true, true, true }))
			return R.raw.auddemoodi_svm;
		if (Arrays.equals(chosen, new boolean[] { false, true, true, true, true }))
			return R.raw.actdemoodi_svm;

		// Five inputs.
		if (Arrays.equals(chosen, new boolean[] { true, true, true, true, true }))
			return R.raw.actauddemoodi_svm;

		return 0;
	}
}
