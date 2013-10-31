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
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PreRecordingChecklist extends SleepApActivity {

	private TextView allDoneText;
	private CheckBox completedQuestionnaire;
	private CheckBox bluetoothMode;
	private CheckBox bluetoothPaired;
	private CheckBox silentMode;
	private CheckBox enoughSpace, enoughBattery;
	private Button nextButton;
	private AudioManager audioManager;
	private int originalRingerMode;
	private BluetoothAdapter bluetoothAdapter;
	private SharedPreferences sharedPreferences;
	private ArrayAdapter<String> arrayAdapter;
	private List<BluetoothDevice> bluetoothDevices;
	private boolean bluetoothReceiverRegistered = false;
	private boolean batteryReceiverRegistered = false;
	private Bundle extras;

	private final BroadcastReceiver bluetoothDiscoveryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				bluetoothDevices.add(device);
				arrayAdapter.add(device.getName() + " - " + device.getAddress());
			}
		}
	};

	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Stop recording if battery level is less than 5%.
			int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			float percentage = (float) batteryLevel / (float) batteryScale;
			enoughBattery.setChecked(percentage >= Constants.PARAM_REQUIRED_BATTERY);
			updateAllDoneText();
		}
	};

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.prerecording_checklist);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		allDoneText = (TextView) findViewById(R.id.allDone);
		completedQuestionnaire = (CheckBox) findViewById(R.id.completedQuestionnaire);
		silentMode = (CheckBox) findViewById(R.id.silentMode);
		bluetoothMode = (CheckBox) findViewById(R.id.bluetoothMode);
		bluetoothPaired = (CheckBox) findViewById(R.id.bluetoothPaired);
		nextButton = (Button) findViewById(R.id.nextButton);
		enoughSpace = (CheckBox) findViewById(R.id.space);
		enoughBattery = (CheckBox) findViewById(R.id.battery);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		ImageButton questionnaireHelp = (ImageButton) findViewById(R.id.questionnaireHelp);
		ImageButton silentModeHelp = (ImageButton) findViewById(R.id.silentHelp);
		ImageButton bluetoothModeHelp = (ImageButton) findViewById(R.id.bluetoothHelp);
		ImageButton bluetoothPairedHelp = (ImageButton) findViewById(R.id.bluetoothPairedHelp);
		ImageButton spaceHelp = (ImageButton) findViewById(R.id.spaceHelp);
		ImageButton batteryHelp = (ImageButton) findViewById(R.id.batteryHelp);

		questionnaireHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PreRecordingChecklist.this).setTitle(getString(R.string.questionnaireHelpTitle))
						.setMessage(Html.fromHtml(getString(R.string.questionnaireHelpMessage)))
						.setNegativeButton(getString(R.string.ok), null).create().show();
			}
		});

		silentModeHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PreRecordingChecklist.this).setTitle(getString(R.string.silentModeHelpTitle))
						.setMessage(Html.fromHtml(getString(R.string.silentModeHelpMessage)))
						.setNegativeButton(getString(R.string.ok), null).create().show();
			}
		});

		bluetoothModeHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PreRecordingChecklist.this).setTitle(getString(R.string.bluetoothModeHelpTitle))
						.setMessage(Html.fromHtml(getString(R.string.bluetoothModeHelpMessage)))
						.setNegativeButton(getString(R.string.ok), null).create().show();
			}
		});

		bluetoothPairedHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PreRecordingChecklist.this).setTitle(getString(R.string.bluetoothPairedHelpTitle))
				.setView(LayoutInflater.from(PreRecordingChecklist.this).inflate(R.layout.pairing_help, null))
				.setNegativeButton(getString(R.string.ok), null).create().show();
			}
		});

		spaceHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PreRecordingChecklist.this).setTitle(getString(R.string.spaceHelpTitle))
						.setMessage(Html.fromHtml(getString(R.string.spaceHelpMessage))).setNegativeButton(getString(R.string.ok), null)
						.create().show();
			}
		});

		batteryHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PreRecordingChecklist.this).setTitle(getString(R.string.batteryHelpTitle))
						.setMessage(Html.fromHtml(getString(R.string.batteryHelpMessage))).setNegativeButton(getString(R.string.ok), null)
						.create().show();
			}
		});

		extras = getIntent().getBundleExtra(Constants.EXTRA_RECORDING_SETTINGS);

		// If phone is on silent, update the checkbox.
		originalRingerMode = audioManager.getRingerMode();
		silentMode.setChecked(originalRingerMode == AudioManager.RINGER_MODE_SILENT);

		// Check questionnaire checkbox if user has filled it out.
		completedQuestionnaire.setChecked(userHasDoneQuestionnaire());

		if (sharedPreferences.getBoolean(Constants.PREF_CHECK_SPACEANDBATTERY, Constants.DEFAULT_CHECK_SPACEANDBATTERY)) {
			// Checks are enabled. If there's enough free space/battery, update
			// the checkboxes.
			enoughSpace.setChecked(isEnoughSpace());
			registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			batteryReceiverRegistered = true;
		} else {
			// Checks are disabled.
			enoughSpace.setClickable(false);
			enoughSpace.setEnabled(false);
			enoughSpace.setText(getString(R.string.spaceCheckDisabled));
			enoughBattery.setClickable(false);
			enoughBattery.setEnabled(false);
			enoughBattery.setText(getString(R.string.batteryCheckDisabled));
		}

		if (extras.getBoolean(Constants.EXTRA_COLLECT_PPG)) {
			if (bluetoothIsAvailable()) {
				// Check bluetooth checkbox if Bluetooth is on.
				String macAddress = sharedPreferences.getString(Constants.PREF_MAC_ADDRESS, Constants.DEFAULT_MAC_ADDRESS);
				bluetoothMode.setChecked(bluetoothIsOn());
				bluetoothPaired.setChecked(deviceIsPaired(macAddress));
			} else {
				// Don't let the user try to turn Bluetooth on - it is
				// unavailable.
				bluetoothMode.setClickable(false);
				bluetoothMode.setEnabled(false);
				bluetoothMode.setText(getString(R.string.bluetoothUnavailable1));
				bluetoothPaired.setClickable(false);
				bluetoothPaired.setEnabled(false);
				bluetoothPaired.setText(getString(R.string.bluetoothUnavailable2));
			}
		} else {
			// Don't need Bluetooth as user has not asked for PPG.
			findViewById(R.id.bluetoothModeContainer).setVisibility(View.GONE);
			findViewById(R.id.bluetoothPairedContainer).setVisibility(View.GONE);
		}

		enoughBattery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Don't let the user check/uncheck this box, it's dealt with
				// using a BroadcastReceiver.
				if (!enoughBattery.isChecked()) {
					// If user has just unchecked the box, recheck it.
					enoughBattery.setChecked(true);
				} else {
					enoughBattery.setChecked(false);
					Toast.makeText(getApplicationContext(), getString(R.string.notEnoughBattery), Toast.LENGTH_LONG).show();
				}
				enoughBattery.setChecked(enoughBattery.isChecked());
				updateAllDoneText();
			}
		});

		enoughSpace.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isEnoughSpace()) {
					enoughSpace.setChecked(true);
				} else {
					enoughSpace.setChecked(false);
					Toast.makeText(getApplicationContext(), getString(R.string.notEnoughSpace), Toast.LENGTH_LONG).show();
				}
				updateAllDoneText();
			}
		});

		completedQuestionnaire.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (completedQuestionnaire.isChecked() && !userHasDoneQuestionnaire()) {
					// User is lying. Tell them, and don't let them check the
					// box.
					Toast.makeText(getApplicationContext(), getString(R.string.noQuestionnaireFound), Toast.LENGTH_LONG).show();
					completedQuestionnaire.setChecked(false);
				}
				updateAllDoneText();
			}
		});

		silentMode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setSilentMode(silentMode.isChecked());
				updateAllDoneText();
			}
		});

		bluetoothMode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (bluetoothMode.isChecked()) {
					bluetoothMode.setChecked(false);
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, Constants.CODE_BLUETOOTH_REQUEST_ENABLE);
				} else {
					bluetoothAdapter.disable();
				}
				updateAllDoneText();
			}
		});

		bluetoothPaired.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (bluetoothPaired.isChecked()) {
					bluetoothPaired.setChecked(false);
					if (bluetoothAdapter.isDiscovering()) {
						bluetoothAdapter.cancelDiscovery();
					}

					arrayAdapter = new ArrayAdapter<String>(PreRecordingChecklist.this, android.R.layout.simple_list_item_1);
					bluetoothDevices = new ArrayList<BluetoothDevice>();

					AlertDialog.Builder builder = new AlertDialog.Builder(PreRecordingChecklist.this);
					builder.setTitle(getString(R.string.discoveringMessage));
					builder.setCancelable(true);
					builder.setNegativeButton(R.string.cancelButtonLabel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int item) {
							bluetoothAdapter.cancelDiscovery();
							BluetoothDevice bluetoothDevice = bluetoothDevices.get(item);
							Method method = null;
							BluetoothSocket bluetoothSocket = null;
							try {
								method = bluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
								bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
							} catch (Exception e) {
								e.printStackTrace();
								return;
							}
							if (bluetoothSocket != null) {
								try {
									bluetoothSocket.connect();
								} catch (Exception e1) {
									e1.printStackTrace();
									try {
										bluetoothSocket.close();
									} catch (Exception e2) {
										e2.printStackTrace();
										return;
									}
									return;
								}
							} else {
								return;
							}
							InputStream inputStream;
							OutputStream outputStream;
							try {
								inputStream = bluetoothSocket.getInputStream();
								outputStream = bluetoothSocket.getOutputStream();
							} catch (IOException e) {
								e.printStackTrace();
								return;
							}
							try {
								inputStream.close();
								outputStream.close();
								bluetoothSocket.close();
							} catch (IOException e) {
							}
							bluetoothPaired.setChecked(true);
							Editor editor = sharedPreferences.edit();
							editor.putString(Constants.PREF_MAC_ADDRESS, bluetoothDevice.getAddress());
							editor.commit();
							updateAllDoneText();
						}
					});

					// Register the BroadcastReceiver
					IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
					registerReceiver(bluetoothDiscoveryReceiver, filter);
					bluetoothReceiverRegistered = true;

					builder.create().show();

					bluetoothAdapter.startDiscovery();
				} else {
					// Don't let the user deselect the bluetoothPaired checkbox.
					// Either it's paired or it's not.
					bluetoothPaired.setChecked(true);
				}
			}
		});

		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View viewNext) {
				boolean usingAudio = extras.getBoolean(Constants.EXTRA_COLLECT_AUDIO);
				boolean usingPpg = extras.getBoolean(Constants.EXTRA_COLLECT_PPG);
				Intent intent;
				if (usingAudio) {
					intent = new Intent(PreRecordingChecklist.this, TutorialMicrophone.class);
				} else {
					if (usingPpg) {
						intent = new Intent(PreRecordingChecklist.this, TutorialPulseOx.class);
					} else {
						intent = new Intent(PreRecordingChecklist.this, TutorialChecks.class);
					}
				}
				extras.putInt(Constants.EXTRA_RING_SETTING, originalRingerMode);
				intent.putExtra(Constants.EXTRA_RECORDING_SETTINGS, extras);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);
			}
		});

		updateAllDoneText();
	}

	@SuppressWarnings("deprecation")
	private boolean isEnoughSpace() {
		StatFs stats = new StatFs(Environment.getExternalStorageDirectory().toString());
		double blocks = stats.getAvailableBlocks();
		double size = stats.getBlockSize();
		double available = blocks * size;
		return available >= Constants.PARAM_REQUIRED_SPACE;
	}

	private boolean bluetoothIsAvailable() {
		if (bluetoothAdapter == null) {
			return false;
		}
		return true;
	}

	private boolean bluetoothIsOn() {
		if (!bluetoothAdapter.isEnabled()) {
			return false;
		}
		return true;
	}

	private boolean userHasDoneQuestionnaire() {
		String questionnaireDirPath = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name)
				+ "/" + Constants.FILENAME_QUESTIONNAIRE;
		return (new File(questionnaireDirPath)).exists();
	}

	protected void updateAllDoneText() {
		// Allow user to proceed if all checkboxes are ticked. If PPG was not
		// chosen, we don't need to ensure bluetooth is enabled.
		boolean happyWithBluetoothBoxes = !(extras.getBoolean(Constants.EXTRA_COLLECT_PPG))
				|| (bluetoothMode.isChecked() && bluetoothPaired.isChecked());
		boolean happyWithRecordingChecks = !(sharedPreferences.getBoolean(Constants.PREF_CHECK_SPACEANDBATTERY,
				Constants.DEFAULT_CHECK_SPACEANDBATTERY)) || enoughBattery.isChecked() && enoughSpace.isChecked();
		boolean happyWithOtherBoxes = completedQuestionnaire.isChecked() && silentMode.isChecked();
		if (happyWithBluetoothBoxes && happyWithOtherBoxes && happyWithRecordingChecks) {
			allDoneText.setText(getString(R.string.readyToRecord));
			nextButton.setEnabled(true);
			nextButton.setClickable(true);
		} else {
			allDoneText.setText(getText(R.string.checkAllBoxes));
			nextButton.setEnabled(false);
			nextButton.setClickable(false);
		}
	}

	public void setSilentMode(boolean value) {
		if (value) {
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		} else {
			audioManager.setRingerMode(originalRingerMode);
		}
	}

	private boolean deviceIsPaired(String inputMacAddress) {
		Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
		if (bondedDevices.size() > 0) {
			for (BluetoothDevice compare : bondedDevices) {
				String compareMacAddress = compare.getAddress();
				if (compareMacAddress.equalsIgnoreCase(inputMacAddress)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.CODE_BLUETOOTH_REQUEST_ENABLE) {
			if (bluetoothIsAvailable() && bluetoothIsOn()) {
				bluetoothMode.setChecked(true);
				Toast.makeText(getApplicationContext(), getString(R.string.bluetoothTurnedOn), Toast.LENGTH_SHORT).show();
				String macAddress = sharedPreferences.getString(Constants.PREF_MAC_ADDRESS, Constants.DEFAULT_MAC_ADDRESS);
				bluetoothPaired.setChecked(deviceIsPaired(macAddress));
			} else {
				bluetoothMode.setChecked(false);
				Toast.makeText(getApplicationContext(), getString(R.string.bluetoothNotTurnedOn), Toast.LENGTH_LONG).show();
				bluetoothPaired.setChecked(false);
			}
		}
		updateAllDoneText();
	}

	@Override
	protected void onDestroy() {
		if (bluetoothReceiverRegistered) {
			unregisterReceiver(bluetoothDiscoveryReceiver);
		}
		if (batteryReceiverRegistered) {
			unregisterReceiver(batteryLevelReceiver);
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.enteringfromleft, R.anim.exitingtoright);
	}
}
