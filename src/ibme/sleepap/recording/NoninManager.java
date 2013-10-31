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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class NoninManager extends Thread {
	private BluetoothAdapter bluetoothAdapter;
	private String macAddress;
	private Handler uiHandler;
	private boolean active = false;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BluetoothSocket bluetoothSocket;
	private Queue<Double> ppgQueue;
	private Queue<Double> spo2Queue;
	private SharedPreferences sharedPreferences;

	public NoninManager(Context context, BluetoothAdapter btAdapter, String macAddress, Handler guiHandler) {
		this.bluetoothAdapter = btAdapter;
		this.macAddress = macAddress;
		this.uiHandler = guiHandler;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void run() {
		active = true;
		int packetSize = 5;
		boolean bluetoothConnected = false;

		ppgQueue = new LinkedList<Double>();
		spo2Queue = new LinkedList<Double>();

		// Check thread has not been paused/stopped.
		if (!active) {
			closeConnections();
			return;
		}

		// Attempt to connect to paired device and start bluetoothstream
		if (setupBluetoothConnectionToDevice()) {
			bluetoothConnected = setNoninOperationMode("D7");
		}

		if (!bluetoothConnected) {
			uiHandler.sendEmptyMessage(Constants.CODE_BLUETOOTH_CONNECTION_UNSUCCESSFUL);
			return;
		} else {

			// Once bluetoothSocket is connected and input/output streams are
			// created, start reading and sending data to the handler.
			uiHandler.sendEmptyMessage(Constants.CODE_BLUETOOTH_CONNECTION_SUCCESSFUL);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			byte[] frameBuffer = new byte[packetSize];
			for (int n = 0; n < frameBuffer.length; n++) {
				frameBuffer[n] = 0;
			}
			NoninPacket packet = new NoninPacket();
			while (active) {
				try {
					// Read the next byte of data from the input stream. This
					// method blocks until input data is available.
					bufferedInputStream.read(frameBuffer);

					if (NoninPacket.isValidFrame(frameBuffer)) {
						if (NoninPacket.isSyncFrame(frameBuffer)) {
							packet.clearPacket();
						}
						packet.addFrame(frameBuffer);
						if (packet.isFull()) {
							double[] ppgWave = packet.getPpgVals();
							List<Double> ppgWaveBoxed = new ArrayList<Double>();
							for (int i = 0; i < ppgWave.length; i++) {
								ppgWaveBoxed.add(Double.valueOf(ppgWave[i]));
							}
							ppgQueue.addAll(ppgWaveBoxed);
							int secsToDisplay = Integer.parseInt(sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS,
									Constants.DEFAULT_GRAPH_RANGE));
							int numberExtraPpgSamples = ppgQueue.size() - secsToDisplay * Constants.PARAM_SAMPLERATE_PPG;
							if (numberExtraPpgSamples > 0) {
								for (int i = 1; i < numberExtraPpgSamples; i++) {
									ppgQueue.remove();
								}
							}
							double spo2 = packet.getSpo2();
							spo2Queue.add(spo2);
							int numberExtraSpo2Samples = spo2Queue.size() - secsToDisplay * Constants.PARAM_SAMPLERATE_SPO2;
							if (numberExtraSpo2Samples > 0) {
								for (int i = 1; i < numberExtraSpo2Samples; i++) {
									spo2Queue.remove();
								}
							}
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putDouble("spo2", spo2);
							bundle.putDoubleArray("ppgArray", ppgWave);
							message.setData(bundle);
							message.what = Constants.CODE_BLUETOOTH_PACKET_RECEIVED;
							uiHandler.sendMessage(message);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			closeConnections();
		}
	}

	private boolean setupBluetoothConnectionToDevice() {
		// Close any open streams/bluetooth devices.
		closeConnections();

		BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
		Method method = null;
		bluetoothSocket = null;
		try {
			method = bluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
			bluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// Connect to Socket, return success/failure.
		boolean connected = connectBluetoothSocket();
		// Initiate input and output streams, return success/failure.
		boolean initiated = initialiseConnections();

		return (connected && initiated);
	}

	private boolean connectBluetoothSocket() {
		if (bluetoothSocket != null) {
			try {
				bluetoothSocket.connect();
				return true;
			} catch (Exception e1) {
				e1.printStackTrace();
				try {
					bluetoothSocket.close();
				} catch (Exception e2) {
					e2.printStackTrace();
					return false;
				}
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean initialiseConnections() {
		try {
			inputStream = bluetoothSocket.getInputStream();
			outputStream = bluetoothSocket.getOutputStream();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean setNoninOperationMode(String controlMessage) {
		byte[] msgBuffer = controlMessage.getBytes();
		try {
			outputStream.write(msgBuffer);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	void closeConnections() {
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (IOException ioe) {
			Log.e(NoninManager.class.toString(), "setupDevices() Unable to close input stream", ioe);
		}

		try {
			if (outputStream != null)
				outputStream.close();
		} catch (IOException ioe) {
			Log.e(NoninManager.class.toString(), "setupDevices() Unable to close output stream", ioe);
		}

		try {
			if (bluetoothSocket != null)
				bluetoothSocket.close();
		} catch (IOException ioe) {
			Log.e(NoninManager.class.toString(), "setupDevices() Unable to close bluetooth socket", ioe);
		}
	}

	public void prepareToStop() {
		active = false;
	}

	public boolean isRunning() {
		return active;
	}

	public Queue<Double> getPpgQueue() {
		return ppgQueue;
	}
}
