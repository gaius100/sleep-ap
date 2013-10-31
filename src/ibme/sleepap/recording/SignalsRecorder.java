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

package ibme.sleepap.recording;

import ibme.sleepap.Constants;
import ibme.sleepap.CustomExceptionHandler;
import ibme.sleepap.MainMenu;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.Utils;
import ibme.sleepap.analysis.ChooseData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.SimpleXYSeries.ArrayFormat;
import com.androidplot.xy.XYPlot;

public class SignalsRecorder extends SleepApActivity implements SensorEventListener {

	private File orientationFile, accelerationFile, actigraphyFile, audioProcessedFile, audioRawFile, bodyPositionFile, ppgFile, spo2File;
	private Position position = Position.Supine;
	int oldPositionValue = Constants.CODE_POSITION_SUPINE;
	private SensorManager sensorManager;
	private Sensor accelerometer, magnetometer;
	long accelerometerCurrentTime = 0;
	long lastAccelerometerReadTime = 0;
	long[] totalPositionTime = new long[5];
	long lastPositionChangeTime;
	long lastAccelerometerRecordedTime;
	private float[] latestAccelerometerEventValues = new float[3];
	private float[] runningGravityComponents = new float[3];
	private float[] previousXAccels = new float[4];
	private float[] previousYAccels = new float[4];
	private float[] previousZAccels = new float[4];
	private double gravitySum = 0;
	private double gravitySquaredSum = 0;
	private int varianceCounter = 0;
	private float[] mGeoMags = new float[3];
	private float[] mOrientation = new float[3];
	private float[] mRotationM = new float[9];
	private String dateTimeString;
	private String filesDirPath;
	private NotificationManager notificationManager;
	private Queue<Double> actigraphyQueue;
	private UserInterfaceUpdater graphUpdateTask;
	private SharedPreferences sharedPreferences;
	private TextView positionDisplay;
	private ExtAudioRecorder extAudioRecorder;
	private WakeLock wakeLock;
	private BluetoothAdapter bluetoothAdapter;
	private NoninManager noninManager;
	private AlertDialog delayAlertDialog;
	private Calendar startTime;
	private boolean startRecordingFlag;
	private boolean finishRecordingFlag;
	private Button reconnectButton;
	private ImageView recordingSign;
	private Bundle extras;
	private boolean screenLocked;
	private boolean actigraphyEnabled, audioEnabled, ppgEnabled;
	private int recordingStartDelayMs, recordingDurationMs;

	private enum Position {
		Supine, Prone, Left, Right, Sitting
	}

	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Stop recording if battery level is less than 5%.
			int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			float percentage = (float) batteryLevel / (float) batteryScale;
			if (percentage < Constants.PARAM_BATTERY_NOTIFICATION_THRESHOLD) {
				stopRecording();
			}
		}
	};

	private BroadcastReceiver bluetoothDisconnectReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) || action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
				Toast.makeText(getApplicationContext(), getString(R.string.bluetoothConnectionLost), Toast.LENGTH_LONG).show();
				reconnectButton.setClickable(true);
				reconnectButton.setEnabled(true);
				if (noninManager != null && noninManager.isRunning()) {
					noninManager.prepareToStop();
				}
				return;
			}
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				Toast.makeText(getApplicationContext(), getString(R.string.bluetoothConnected), Toast.LENGTH_SHORT).show();
				return;
			}
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				// Bluetooth has been turned on/off.
				int bluetoothState = BluetoothAdapter.getDefaultAdapter().getState();
				if (bluetoothState == BluetoothAdapter.STATE_ON) {
					Toast.makeText(getApplicationContext(), getString(R.string.bluetoothTurnedBackOn), Toast.LENGTH_LONG).show();
					reconnectButton.setClickable(true);
					reconnectButton.setEnabled(true);
					if (noninManager != null && noninManager.isRunning()) {
						noninManager.prepareToStop();
					}
					return;
				}
				if (bluetoothState == BluetoothAdapter.STATE_OFF) {
					Toast.makeText(getApplicationContext(), getString(R.string.bluetoothTurnedOff), Toast.LENGTH_LONG).show();
					reconnectButton.setClickable(false);
					reconnectButton.setEnabled(false);
					if (noninManager != null && noninManager.isRunning()) {
						noninManager.prepareToStop();
					}
					return;
				}
			}
		}
	};

	private static class PpgHandler extends Handler {
		private final WeakReference<SignalsRecorder> weakReference;

		public PpgHandler(SignalsRecorder signalsRecorder) {
			this.weakReference = new WeakReference<SignalsRecorder>(signalsRecorder);
		}

		@Override
		public void handleMessage(Message message) {
			SignalsRecorder activityReference = weakReference.get();
			switch (message.what) {
			case Constants.CODE_BLUTOOTH_NO_PAIRED_DEVICES:
				Toast.makeText(activityReference.getApplicationContext(), activityReference.getString(R.string.noPairedDevices), Toast.LENGTH_LONG).show();
				break;
			case Constants.CODE_BLUETOOTH_CONNECTION_UNSUCCESSFUL:
				Toast.makeText(activityReference.getApplicationContext(), activityReference.getString(R.string.bluetoothConnectionUnsuccessful),
						Toast.LENGTH_LONG).show();
				activityReference.reconnectButton.setClickable(true);
				activityReference.reconnectButton.setEnabled(true);
				break;
			case Constants.CODE_BLUETOOTH_CONNECTION_SUCCESSFUL:
				Toast.makeText(activityReference.getApplicationContext(), activityReference.getString(R.string.bluetoothConnected), Toast.LENGTH_SHORT).show();
				break;
			case Constants.CODE_BLUETOOTH_PACKET_RECEIVED:
				// Write PPG and SpO2 data to file.
				Bundle bundle = message.getData();
				double spo2 = bundle.getDouble("spo2");
				double[] ppgWave = bundle.getDoubleArray("ppgArray");
				long bundleStartTime = System.currentTimeMillis();
				if (!activityReference.startRecordingFlag) {
					// Don't write to file.
					break;
				}
				try {
					BufferedWriter ppgBufferedWriter = new BufferedWriter(new FileWriter(activityReference.ppgFile, true));
					double timestamp = bundleStartTime;
					// TODO: sort out these timestamps properly: the below
					// is a dirty hack. Maybe timestamp each individual
					// frame as it arrives?

					// It's the packet that's timestamped (i.e. we have here
					// 25 frames, and we're just getting a timestamp now).
					// So artificially create the other timestamps by adding
					// 1/75 seconds after each frame is written.
					for (double val : ppgWave) {
						ppgBufferedWriter.append(String.valueOf(Math.round(timestamp)) + ",");
						ppgBufferedWriter.append(String.valueOf(val) + "\n");
						timestamp += 13.33;
					}
					ppgBufferedWriter.flush();
					ppgBufferedWriter.close();
				} catch (IOException e) {
					Log.e(Constants.CODE_APP_TAG, "Error writing PPG data to file", e);
				}
				try {
					BufferedWriter spo2BufferedWriter = new BufferedWriter(new FileWriter(activityReference.spo2File, true));
					spo2BufferedWriter.append(String.valueOf(bundleStartTime) + ",");
					spo2BufferedWriter.append(String.valueOf(spo2) + "\n");
					spo2BufferedWriter.flush();
					spo2BufferedWriter.close();
				} catch (IOException e) {
					Log.e(Constants.CODE_APP_TAG, "Error writing SpO2 data to file", e);
				}
				break;
			}
		}
	};

	private class UserInterfaceUpdater extends AsyncTask<Void, Void, String> {
		private XYPlot _activityPlot;
		private XYPlot _audioPlot;
		private XYPlot _ppgPlot;
		private boolean _stopRunningFlag;
		private int _counter;
		private PointLabelFormatter _plf;
		private LineAndPointFormatter _activityFormatter;
		private LineAndPointFormatter _audioFormatter;
		private LineAndPointFormatter _ppgFormatter;
		private XYSeries _ppgSeries;
		private XYSeries _activitySeries;
		private XYSeries _audioSeries;

		@Override
		protected String doInBackground(Void... params) {
			_activityPlot = (XYPlot) findViewById(R.id.activityPlot);
			_audioPlot = (XYPlot) findViewById(R.id.audioPlot);
			_ppgPlot = (XYPlot) findViewById(R.id.ppgPlot);
			_plf = new PointLabelFormatter(getResources().getColor(R.color.transparent));
			_activityFormatter = new LineAndPointFormatter(getResources().getColor(R.color.darkgreen), null, getResources().getColor(
					R.color.translucentDarkGreen), _plf);
			_audioFormatter = new LineAndPointFormatter(Color.BLUE, null, getResources().getColor(R.color.translucentBlue), _plf);
			_ppgFormatter = new LineAndPointFormatter(Color.RED, null, getResources().getColor(R.color.translucentRed), _plf);

			if (actigraphyEnabled) {
				initialisePlot(_activityPlot);
			} else {
				((LinearLayout) findViewById(R.id.activityDisplay)).setVisibility(View.GONE);
			}
			if (audioEnabled) {
				initialisePlot(_audioPlot);
			} else {
				((LinearLayout) findViewById(R.id.audioDisplay)).setVisibility(View.GONE);
			}
			if (ppgEnabled) {
				initialisePlot(_ppgPlot);
			} else {
				((LinearLayout) findViewById(R.id.ppgDisplay)).setVisibility(View.GONE);
			}

			_stopRunningFlag = false;
			while (!_stopRunningFlag) {
				try {
					Thread.sleep(Constants.PARAM_UI_UPDATE_PERIOD);
				} catch (InterruptedException e) {
					Log.e(Constants.CODE_APP_TAG, "InterruptedException during UI thread sleep", e);
					continue;
				}

				// Check every minute...
				if (_counter == Constants.PARAM_FLAGS_CHECK_PERIOD) {
					Calendar now = Calendar.getInstance(Locale.getDefault());
					long timeSinceStartMillis = now.getTimeInMillis() - startTime.getTimeInMillis();
					if (!startRecordingFlag) {
						// Should we be recording yet?
						if (timeSinceStartMillis > recordingStartDelayMs) {
							startRecordingFlag = true;
							if (audioEnabled) {
								// Got to tell extAudioRecorder as it's in a
								// separate class and can't see
								// startRecordingFlag.
								extAudioRecorder.setShouldWrite(true);
							}
							if (delayAlertDialog != null) {
								delayAlertDialog.cancel();
							}
							// Notify user.
							if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS, Constants.DEFAULT_NOTIFICATIONS)) {
								NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
										.setSmallIcon(R.drawable.notification_icon)
										.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.deviceaccessmic)).setContentTitle("SleepAp")
										.setContentText(getString(R.string.startedRecordingNotification)).setAutoCancel(true);
								notificationManager.notify(Constants.CODE_APP_NOTIFICATION_ID, builder.build());
							}
						}
					} else {
						// We have started - should we finish now?
						if (timeSinceStartMillis > recordingStartDelayMs + recordingDurationMs) {
							finishRecordingFlag = true;
						}
					}
					_counter = 0;
				}
				_counter++;
				publishProgress();
			}
			return "Stopped";
		}

		private void initialisePlot(XYPlot plot) {
			Paint whitePaint = new Paint();
			whitePaint.setColor(Color.WHITE);
			whitePaint.setAlpha(255);
			plot.getLayoutManager().remove(plot.getLegendWidget());
			plot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
			plot.setBorderPaint(null);
			plot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);
			plot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
			plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.WHITE);
			plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.WHITE);
			plot.getGraphWidget().getBackgroundPaint().setAlpha(0);
			plot.getGraphWidget().setGridBackgroundPaint(whitePaint);
			plot.setDomainLabel("");
			plot.setRangeLabel("");
			plot.setTitle("");
			plot.getGraphWidget().getRangeLabelPaint().setAlpha(0);
			plot.getGraphWidget().getDomainLabelPaint().setAlpha(0);
			plot.getGraphWidget().getDomainOriginLabelPaint().setAlpha(0);
			plot.getGraphWidget().getRangeOriginLabelPaint().setAlpha(0);
			plot.setPlotPadding(-60, -22, -5, -35); // L,T,R,B
			plot.setMarkupEnabled(false);
			plot.getGraphWidget().getGridLinePaint().setAlpha(0);
		}

		@Override
		protected void onProgressUpdate(Void... progress) {

			if (finishRecordingFlag) {
				stopRecording();
			} else {
				if (startRecordingFlag && recordingSign.getVisibility() == View.GONE) {
					recordingSign.setVisibility(View.VISIBLE);
				}
			}

			if (!screenLocked) {
				// Only bother updating the UI if the screen is currently
				// unlocked.
				try {
					// Activity.
					if (actigraphyEnabled) {
						List<Number> activityVals = doubleQueueToNumberList(actigraphyQueue);
						_activitySeries = new SimpleXYSeries(activityVals, ArrayFormat.Y_VALS_ONLY, "");
						_activityPlot.removeSeries(_activitySeries);
						_activityPlot.clear();
						_activityPlot.addSeries(_activitySeries, _activityFormatter);
						_activityPlot.redraw();
						_activityPlot.setRangeBoundaries(Constants.PARAM_ACTIVITY_GRAPH_MIN_Y, BoundaryMode.FIXED, Constants.PARAM_ACTIVITY_GRAPH_MAX_Y,
								BoundaryMode.FIXED);
					}

					// Position.
					String positionText;
					if (position == Position.Prone) {
						positionText = "On front";
					} else if (position == Position.Supine) {
						positionText = "On back";
					} else {
						positionText = position.toString();
					}
					positionDisplay.setText(getString(R.string.estimatedPosition) + " " + positionText);

					// Audio.
					if (audioEnabled) {
						Queue<Double> audioQueue = extAudioRecorder.getAudioQueue();
						List<Number> audioVals = doubleQueueToNumberList(audioQueue);
						_audioSeries = new SimpleXYSeries(audioVals, ArrayFormat.Y_VALS_ONLY, "");
						_audioPlot.removeSeries(_audioSeries);
						_audioPlot.clear();
						_audioPlot.addSeries(_audioSeries, _audioFormatter);
						_audioPlot.redraw();
						_audioPlot.setRangeBoundaries(Constants.PARAM_AUDIO_GRAPH_MIN_Y, BoundaryMode.FIXED, Constants.PARAM_AUDIO_GRAPH_MAX_Y,
								BoundaryMode.FIXED);
					}

					// PPG.
					if (ppgEnabled) {
						List<Number> ppgVals = doubleQueueToNumberList(noninManager.getPpgQueue());
						_ppgSeries = new SimpleXYSeries(ppgVals, ArrayFormat.Y_VALS_ONLY, "");
						_ppgPlot.removeSeries(_ppgSeries);
						_ppgPlot.clear();
						_ppgPlot.addSeries(_ppgSeries, _ppgFormatter);
						_ppgPlot.redraw();
					}

				} catch (OutOfMemoryError e) {
					Log.e(Constants.CODE_APP_TAG, "OutOfMemoryError during UI update", e);
					resetUi();
				}
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("Stopped")) {
				// Thread was cancelled.
			}
		}

		public void stopUiUpdates() {
			_stopRunningFlag = true;
		}

		public boolean isRunning() {
			return !_stopRunningFlag;
		}
	}

	/**
	 * Called when the activity is first created. onStart() is called
	 * immediately afterwards.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signals_recorder);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		extras = getIntent().getBundleExtra(Constants.EXTRA_RECORDING_SETTINGS);
		actigraphyEnabled = extras.getBoolean(Constants.EXTRA_COLLECT_ACTIGRAPHY, false);
		audioEnabled = extras.getBoolean(Constants.EXTRA_COLLECT_AUDIO, false);
		ppgEnabled = extras.getBoolean(Constants.EXTRA_COLLECT_PPG, false);
		dateTimeString = DateFormat.format(Constants.PARAM_DATE_FORMAT, System.currentTimeMillis()).toString();
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		String appDirPath = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name);
		filesDirPath = appDirPath + "/" + dateTimeString + "/";
		lastAccelerometerRecordedTime = 0;
		lastPositionChangeTime = System.currentTimeMillis();
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.CODE_APP_TAG);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		actigraphyQueue = new LinkedList<Double>();
		positionDisplay = (TextView) findViewById(R.id.position);
		recordingSign = (ImageView) findViewById(R.id.recordingSign);

		for (int i = 0; i < 5; ++i) {
			totalPositionTime[i] = 0;
		}

		// Battery check receiver.
		registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		// Button to stop the recording.
		Button stopButton = (Button) findViewById(R.id.buttonStop);
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View viewNext) {
				stopRecording();
			}
		});

		// Button to reconnect the bluetooth.
		reconnectButton = (Button) findViewById(R.id.reconnectButton);
		reconnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View viewNext) {
				String macAddress = sharedPreferences.getString(Constants.PREF_MAC_ADDRESS, Constants.DEFAULT_MAC_ADDRESS);
				noninManager = null;
				noninManager = new NoninManager(getApplicationContext(), bluetoothAdapter, macAddress, new PpgHandler(SignalsRecorder.this));
				noninManager.start();
				reconnectButton.setEnabled(false);
				reconnectButton.setClickable(false);
			}
		});

		// Create a folder for the recordings, and delete any extra recordings.
		File dir = new File(filesDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
			File appDir = new File(appDirPath);

			// Create a list of recordings in the app directory. These
			// are named by the date on which they were formed and so can be in
			// date order (earliest first).
			String[] recordingDirs = appDir.list();
			Arrays.sort(recordingDirs);

			// How many more recordings do we have in the app directory than are
			// specified in the settings? Should account for questionnaires
			// file,
			// which must exist for the user to have gotten to this stage
			// (checklist).

			int numberRecordings = 0;
			for (String folderOrFileName : recordingDirs) {
				if (!folderOrFileName.equals(Constants.FILENAME_QUESTIONNAIRE) && !folderOrFileName.equals(Constants.FILENAME_LOG_DIRECTORY)
						&& !folderOrFileName.equals(Constants.FILENAME_FEEDBACK_DIRECTORY)) {
					numberRecordings++;
				}
			}

			int extraFiles = numberRecordings
					- Integer.parseInt(sharedPreferences.getString(Constants.PREF_NUMBER_RECORDINGS, Constants.DEFAULT_NUMBER_RECORDINGS));

			if (extraFiles > 0) {
				// Too many recordings. Delete the earliest n, where n is the
				// number of extra files.
				boolean success;
				int nDeleted = 0;
				for (String candidateFolderName : recordingDirs) {
					if (nDeleted >= extraFiles) {
						// We've deleted enough already.
						break;
					}
					if (candidateFolderName.equals(Constants.FILENAME_QUESTIONNAIRE) || candidateFolderName.equals(Constants.FILENAME_LOG_DIRECTORY)
							|| candidateFolderName.equals(Constants.FILENAME_FEEDBACK_DIRECTORY)) {
						// Don't delete questionnaire file or log/feedback
						// directory.
						continue;
					}
					// See if the path is a directory, and skip it if it isn't.
					File candidateFolder = new File(appDir, candidateFolderName);
					if (!candidateFolder.isDirectory()) {
						continue;
					}
					// If we've got to this stage, the file is the earliest
					// recording and should be deleted. Delete files in
					// recording first.
					success = Utils.deleteDirectory(candidateFolder);
					if (success) {
						nDeleted++;
					}
				}
			}
		}

		// Copy latest questionnaire File
		try {
			File latestQuestionnaireFile = new File(appDirPath, Constants.FILENAME_QUESTIONNAIRE);
			InputStream in = new FileInputStream(latestQuestionnaireFile);
			OutputStream out = new FileOutputStream(new File(filesDirPath, Constants.FILENAME_QUESTIONNAIRE));
			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(Constants.CODE_APP_TAG, "FileNotFoundException copying Questionnaire file.");
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG, "IOException copying Questionnaire file.");
		}

		// Create txt files.
		orientationFile = new File(filesDirPath, Constants.FILENAME_ORIENTATION);
		accelerationFile = new File(filesDirPath, Constants.FILENAME_ACCELERATION_RAW);
		actigraphyFile = new File(filesDirPath, Constants.FILENAME_ACCELERATION_PROCESSED);
		audioProcessedFile = new File(filesDirPath, Constants.FILENAME_AUDIO_PROCESSED);
		bodyPositionFile = new File(filesDirPath, Constants.FILENAME_POSITION);
		ppgFile = new File(filesDirPath, Constants.FILENAME_PPG);
		spo2File = new File(filesDirPath, Constants.FILENAME_SPO2);
		audioRawFile = new File(filesDirPath, Constants.FILENAME_AUDIO_RAW);

		/** Recording starts here. */
		// Log start time so recording can begin in 30 minutes.
		startTime = Calendar.getInstance(Locale.getDefault());
		finishRecordingFlag = false;
		recordingStartDelayMs = Constants.CONST_MILLIS_IN_MINUTE
				* Integer.parseInt(sharedPreferences.getString(Constants.PREF_RECORDING_START_DELAY, Constants.DEFAULT_RECORDING_START_DELAY));
		recordingDurationMs = Constants.CONST_MILLIS_IN_MINUTE
				* Integer.parseInt(sharedPreferences.getString(Constants.PREF_RECORDING_DURATION, Constants.DEFAULT_RECORDING_DURATION));
		if (recordingStartDelayMs > 0) {
			startRecordingFlag = false;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder
					.setTitle(getString(R.string.delayAlertTitle))
					.setMessage(
							getString(R.string.delayAlertMessage1) + " "
									+ sharedPreferences.getString(Constants.PREF_RECORDING_START_DELAY, Constants.DEFAULT_RECORDING_START_DELAY) + " "
									+ getString(R.string.delayAlertMessage2)).setPositiveButton(getString(R.string.ok), null);
			delayAlertDialog = dialogBuilder.create();
			delayAlertDialog.show();
		} else {
			startRecordingFlag = true;
			// Notify user
			Intent notificationIntent = new Intent(SignalsRecorder.this, SignalsRecorder.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS, Constants.DEFAULT_NOTIFICATIONS)) {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.notification_icon)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.deviceaccessmic)).setContentTitle("SleepAp")
						.setContentText(getString(R.string.startedRecordingNotification)).setAutoCancel(false).setOngoing(true).setContentIntent(pendingIntent);
				notificationManager.notify(Constants.CODE_APP_NOTIFICATION_ID, builder.build());
				recordingSign.setVisibility(View.VISIBLE);
			}
		}

		// Start audio recording.
		if (audioEnabled) {
			extAudioRecorder = new ExtAudioRecorder(this);
			extAudioRecorder.setOutputFile(audioRawFile);
			extAudioRecorder.setShouldWrite(startRecordingFlag);
			extAudioRecorder.setAudioProcessedFile(audioProcessedFile);
			extAudioRecorder.prepare();
			extAudioRecorder.start();
		}

		// Start PPG recording.
		if (ppgEnabled && bluetoothAdapter != null) {
			String macAddress = sharedPreferences.getString(Constants.PREF_MAC_ADDRESS, Constants.DEFAULT_MAC_ADDRESS);
			noninManager = new NoninManager(this, bluetoothAdapter, macAddress, new PpgHandler(SignalsRecorder.this));
			noninManager.start();
		}

		// Start actigraphy recording.
		if (actigraphyEnabled) {
			sensorManager.registerListener(this, accelerometer,
					1000000 / (Constants.PARAM_SAMPLERATE_ACCELEROMETER * Constants.PARAM_UPSAMPLERATE_ACCELEROMETER));
			sensorManager.registerListener(this, magnetometer, 1000000 / Constants.PARAM_SAMPLERATE_ACCELEROMETER);
		}
		wakeLock.acquire();

		// Set up listener so that if Bluetooth connection is lost we set give
		// the user an option to reconnect.
		if (ppgEnabled) {
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
			filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

			registerReceiver(bluetoothDisconnectReceiver, filter);
		}

		// Start graphs update.
		graphUpdateTask = new UserInterfaceUpdater();
		graphUpdateTask.execute();
	}

	@Override
	protected void onStart() {
		screenLocked = false;
		super.onStart();
	}

	@Override
	protected void onStop() {
		screenLocked = true;
		super.onStop();
	}

	public void resetUi() {
		graphUpdateTask.stopUiUpdates();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Log.e(Constants.CODE_APP_TAG, "Error while thread sleeping", e);
		}
		System.gc();
		graphUpdateTask = new UserInterfaceUpdater();
		graphUpdateTask.execute();
	}

	protected void stopRecording() {

		// Cancel dialogs.
		if (delayAlertDialog != null) {
			delayAlertDialog.cancel();
		}

		// Unregister listeners.
		try {
			if (ppgEnabled) {
				unregisterReceiver(bluetoothDisconnectReceiver);
			}
		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG, "Error unregistering bluetooth disconnect BroadcastReceiver", e);
		}
		try {
			unregisterReceiver(batteryLevelReceiver);
		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG, "Error unregistering bluetooth disconnect BroadcastReceiver", e);
		}

		// Stop graphs update.
		if (graphUpdateTask.isRunning()) {
			graphUpdateTask.stopUiUpdates();
		}

		// Audio
		if (audioEnabled) {
			extAudioRecorder.stop();
			extAudioRecorder.release();
		}

		// PPG
		if (ppgEnabled) {
			noninManager.prepareToStop();
		}

		// Actigraphy
		if (actigraphyEnabled) {
			sensorManager.unregisterListener(this);
		}

		try {
			wakeLock.release();
		} catch (Throwable t) {
			Log.e(Constants.CODE_APP_TAG, "Wakelock has already been released", t);
		}

		// Check if we have both stopped and started recording properly. If we
		// have, everything has worked properly. If we haven't, user probably
		// interrupted recordings and we should delete the files they made.
		boolean shouldDelete = sharedPreferences.getBoolean(Constants.PREF_EARLY_EXIT_DELETION, Constants.DEFAULT_EARLY_EXIT_DELETION);
		boolean recordingSuccessful = startRecordingFlag && finishRecordingFlag;
		if (shouldDelete && !recordingSuccessful) {
			// User interrupted recording. Recordings are useless - get rid of
			// them.
			if (filesDirPath != null) {
				File filesDir = new File(filesDirPath);
				Utils.deleteDirectory(filesDir);
			}
		} else {
			// Otherwise save the position data - woudn't need to do this if
			// files were to be deleted.
			saveBodyPositionData();
		}

		// Cancel recording notification.
		if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS, Constants.DEFAULT_NOTIFICATIONS)) {
			notificationManager.cancel(Constants.CODE_APP_NOTIFICATION_ID);
		}

		recordingSign.setVisibility(View.GONE);

		// Let the user know the recording is over.
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(getString(R.string.finishedAlertTitle));
		if (shouldDelete && !recordingSuccessful) {
			dialogBuilder.setMessage(getString(R.string.finishedAlertFailure));
		} else {
			dialogBuilder.setMessage(getString(R.string.finishedAlertSuccess));
			dialogBuilder.setNegativeButton(getString(R.string.analyseButtonText), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(SignalsRecorder.this, ChooseData.class);
					File recordingDir = new File(filesDirPath);
					intent.putExtra(Constants.EXTRA_RECORDING_DIRECTORY, recordingDir);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			});
		}
		dialogBuilder.setPositiveButton(getString(R.string.mainMenuButtonText), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(SignalsRecorder.this, MainMenu.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		dialogBuilder.setCancelable(false);
		dialogBuilder.create().show();

		// Change logcat back if necessary.
		if (sharedPreferences.getBoolean(Constants.PREF_WRITE_LOG, Constants.DEFAULT_WRITE_LOG)) {
			String[] cmd = new String[] { "logcat", "-f", "stdout", "*:V" };
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG, "Error changing logcat back to default", e);
			}
		}

		// Reset phone ringing mode to what it was before user changed it to
		// silent.
		AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(extras.getInt(Constants.EXTRA_RING_SETTING));
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			// Checking which type of sensor called this listener
			// In this case it is the Accelerometer (the next is the Magnetomer)
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerCurrentTime = System.currentTimeMillis();

				if (accelerometerCurrentTime - lastAccelerometerReadTime > (1000 / Constants.PARAM_SAMPLERATE_ACCELEROMETER - 1000 / (Constants.PARAM_SAMPLERATE_ACCELEROMETER * Constants.PARAM_UPSAMPLERATE_ACCELEROMETER))) {
					lastAccelerometerReadTime = accelerometerCurrentTime;
					float xRaw = event.values[0];
					float yRaw = event.values[1];
					float zRaw = event.values[2];

					// Extracts unwanted gravity component from the
					// accelerometer signal.
					float alpha = Constants.PARAM_GRAVITY_FILTER_COEFFICIENT;
					runningGravityComponents[0] = runningGravityComponents[0] * alpha + (1 - alpha) * xRaw;
					runningGravityComponents[1] = runningGravityComponents[1] * alpha + (1 - alpha) * yRaw;
					runningGravityComponents[2] = runningGravityComponents[2] * alpha + (1 - alpha) * zRaw;

					float xAccel = xRaw - runningGravityComponents[0];
					float yAccel = yRaw - runningGravityComponents[1];
					float zAccel = zRaw - runningGravityComponents[2];

					double magnitudeSquare = xAccel * xAccel + yAccel * yAccel + zAccel * zAccel;
					double magnitude = Math.sqrt(magnitudeSquare);

					actigraphyQueue.add(magnitude);
					int secsToDisplay = Integer.parseInt(sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS, Constants.DEFAULT_GRAPH_RANGE));
					int numberExtraSamples = actigraphyQueue.size() - (secsToDisplay * Constants.PARAM_SAMPLERATE_ACCELEROMETER);
					if (numberExtraSamples > 0) {
						for (int i = 0; i < numberExtraSamples; i++) {
							actigraphyQueue.remove();
						}
					}

					// Saves accelerometer data, necessary for the orientation
					// computation
					System.arraycopy(event.values, 0, latestAccelerometerEventValues, 0, 3);
					pushBackAccelerometerValues(xRaw, yRaw, zRaw);

					if (accelerometerCurrentTime - lastAccelerometerRecordedTime > Constants.PARAM_ACCELEROMETER_RECORDING_PERIOD + 1000
							/ Constants.PARAM_SAMPLERATE_ACCELEROMETER) {
						if (startRecordingFlag) {
							writeActigraphyLogVariance();
						}
						gravitySum = gravitySquaredSum = 0;
						varianceCounter = 0;
						lastAccelerometerRecordedTime = accelerometerCurrentTime;
					}
					if (startRecordingFlag) {
						writeRawActigraphy();
					}
				}
			}

			// Checking if the Magnetomer called this listener
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

				// Copying magnetometer measures.
				System.arraycopy(event.values, 0, mGeoMags, 0, 3);

				if (SensorManager.getRotationMatrix(mRotationM, null, latestAccelerometerEventValues, mGeoMags)) {
					SensorManager.getOrientation(mRotationM, mOrientation);

					// Finding current orientation requires both Accelerometer
					// (using the previous measure) and Magnetometer data.
					// Converting radians to degrees (yaw, pitch, roll)
					mOrientation[0] = mOrientation[0] * Constants.CONST_DEGREES_PER_RADIAN;
					mOrientation[1] = mOrientation[1] * Constants.CONST_DEGREES_PER_RADIAN;
					mOrientation[2] = mOrientation[2] * Constants.CONST_DEGREES_PER_RADIAN;

					// The values (1,2,3,4) attributed for
					// supine/prone/left/right match the
					// ones attributed in VISI text files.
					int positionValue = 0;
					// Supine (4).
					if (-45 < mOrientation[1] && mOrientation[1] < 45 && -45 < mOrientation[2] && mOrientation[2] < 45) {
						positionValue = Constants.CODE_POSITION_SUPINE;
						position = Position.Supine;
					}

					// Prone (1).
					if ((((-180 < mOrientation[2] && mOrientation[2] < -135) || (135 < mOrientation[2] && mOrientation[2] < 180)) && -45 < mOrientation[1] && mOrientation[1] < 45)) {
						positionValue = Constants.CODE_POSITION_PRONE;
						position = Position.Prone;
					}

					// Right (2).
					if (-90 < mOrientation[2] && mOrientation[2] < -45) {
						positionValue = Constants.CODE_POSITION_RIGHT;
						position = Position.Right;
					}

					// Left (3).
					if (45 < mOrientation[2] && mOrientation[2] < 90) {
						positionValue = Constants.CODE_POSITION_LEFT;
						position = Position.Left;
					}

					// Sitting up (5).
					if ((((-135 < mOrientation[1] && mOrientation[1] < -45) || (45 < mOrientation[1] && mOrientation[1] < 135)) && -45 < mOrientation[2] && mOrientation[2] < 45)) {
						positionValue = Constants.CODE_POSITION_SITTING;
						position = Position.Sitting;
					}

					if ((oldPositionValue != positionValue) && (positionValue != 0) && startRecordingFlag) {
						updatePositionChangeTime(oldPositionValue);
						oldPositionValue = positionValue;
						try {
							// Write raw body position data
							BufferedWriter orientationBufferedWriter = new BufferedWriter(new FileWriter(orientationFile, true));
							orientationBufferedWriter.append(String.valueOf(System.currentTimeMillis()) + ",");
							orientationBufferedWriter.append(String.valueOf(positionValue) + "\n");
							orientationBufferedWriter.flush();
							orientationBufferedWriter.close();
						} catch (IOException e) {
							Log.e(Constants.CODE_APP_TAG, "Error writing orientation data to file", e);
						}
					}
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void pushBackAccelerometerValues(float xAccel, float yAccel, float zAccel) {
		for (int i = 0; i < 3; ++i) {
			previousXAccels[i] = previousXAccels[i + 1];
			previousYAccels[i] = previousYAccels[i + 1];
			previousZAccels[i] = previousZAccels[i + 1];
		}

		previousXAccels[3] = xAccel;
		previousYAccels[3] = yAccel;
		previousZAccels[3] = zAccel;

		float meanXAccel = (xAccel + previousXAccels[2] - previousXAccels[1] - previousXAccels[0]) / 2;
		float meanYAccel = (yAccel + previousYAccels[2] - previousYAccels[1] - previousYAccels[0]) / 2;
		float meanZAccel = (zAccel + previousZAccels[2] - previousZAccels[1] - previousZAccels[0]) / 2;

		double magnitudeSquare = meanXAccel * meanXAccel + meanYAccel * meanYAccel + meanZAccel * meanZAccel;
		double magnitude = Math.sqrt(magnitudeSquare);

		gravitySum += magnitude;
		gravitySquaredSum += magnitudeSquare;
		varianceCounter += 1;
	}

	void writeRawActigraphy() {
		// Writes Accelerometer data
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(accelerationFile, true));
			out.append(String.valueOf(accelerometerCurrentTime) + ",");
			out.append(String.valueOf(latestAccelerometerEventValues[0]) + ",");
			out.append(String.valueOf(latestAccelerometerEventValues[1]) + ",");
			out.append(String.valueOf(latestAccelerometerEventValues[2]) + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG, "Error writing raw actigraphy data to file", e);
		}
	}

	void updatePositionChangeTime(int positionValue) {
		// Updates the time spent on a body position
		long currentTime = System.currentTimeMillis();
		totalPositionTime[positionValue - 1] += currentTime - lastPositionChangeTime;
		lastPositionChangeTime = currentTime;
	}

	void saveBodyPositionData() {
		// Computing full time spent during the session (supine, prone, right,
		// left, sitting)
		int totalTime = (int) (totalPositionTime[0] + totalPositionTime[1] + totalPositionTime[2] + totalPositionTime[3] + totalPositionTime[4]);
		totalTime = Math.max(totalTime, 1);

		// Computing % of time spent in each position
		float supineProportion = ((float) totalPositionTime[Constants.CODE_POSITION_SUPINE - 1] * 100) / totalTime;
		float proneProportion = ((float) totalPositionTime[Constants.CODE_POSITION_PRONE - 1] * 100) / totalTime;
		float rightProportion = ((float) totalPositionTime[Constants.CODE_POSITION_RIGHT - 1] * 100) / totalTime;
		float leftProportion = ((float) totalPositionTime[Constants.CODE_POSITION_LEFT - 1] * 100) / totalTime;
		float sittingProportion = ((float) totalPositionTime[Constants.CODE_POSITION_SITTING - 1] * 100) / totalTime;

		try {
			// Writing data into file
			BufferedWriter out = new BufferedWriter(new FileWriter(bodyPositionFile, true));
			out.write(String.valueOf(supineProportion) + ',' + String.valueOf(proneProportion) + ',' + String.valueOf(leftProportion) + ','
					+ String.valueOf(rightProportion) + ',' + String.valueOf(sittingProportion) + " \n");
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG, "Error writing position data to file", e);
		}
	}

	void writeActigraphyLogVariance() {
		double mean = gravitySum / varianceCounter;
		double logVariance = Math.log(1 + gravitySquaredSum / varianceCounter - mean * mean);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(actigraphyFile, true));
			// out.append(String.valueOf(accelerometerCurrentTime) + ",");
			// out.flush();
			out.write(String.valueOf(logVariance) + ",");
			out.write(String.valueOf(varianceCounter) + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG, "Error writing processed actigraphy data to file", e);
		}
	}

	private List<Number> doubleQueueToNumberList(Queue<Double> queue) {
		List<Number> list;
		// If the Queue is modified while the loop is running (which is more
		// than possible), a ConcurrentModificationException will be thrown.
		// If one is, it is caught and we try again.
		int attempts = 0;
		while (attempts < 5) {
			try {
				list = new ArrayList<Number>();
				for (Iterator<Double> iter = queue.iterator(); iter.hasNext();) {
					Double obj = iter.next();
					list.add(obj);
				}
				return list;
			} catch (ConcurrentModificationException e) {
				// Don't return anything so we go through the while loop again.
				attempts++;
			}
		}
		list = new ArrayList<Number>();
		list.add(0);
		list.add(0);
		return list;
	}

	@Override
	public void onBackPressed() {
		// Check user actually wanted to cancel...
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(getString(R.string.backPressedAlertTitle));
		dialogBuilder.setMessage(getString(R.string.backPressedAlertMessage));
		dialogBuilder.setNegativeButton(getString(R.string.no), null);
		dialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopRecording();
			}
		});
		dialogBuilder.create().show();
	}

	/** Handles option selection. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection. Override SleepApActivity to stop recording if
		// Main menu is pressed.
		switch (item.getItemId()) {
		case R.id.menu_exit:
			stopRecording();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}