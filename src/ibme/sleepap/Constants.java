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

public class Constants {
	// Default settings for user preferences.
	public static final String DEFAULT_MAC_ADDRESS = "00:00:00:00:00:00";
	public static final String DEFAULT_GRAPH_RANGE = "10"; // Seconds
	public static final String DEFAULT_NUMBER_RECORDINGS = "3";
	public static final String DEFAULT_ODI_THRESHOLD = "3";
	public static final String DEFAULT_RECORDING_START_DELAY = "30";
	public static final String DEFAULT_RECORDING_DURATION = "240";
	public static final boolean DEFAULT_NOTIFICATIONS = true;
	public static final boolean DEFAULT_SHOULD_USE_ACTIGRAPHY = true;
	public static final boolean DEFAULT_SHOULD_USE_AUDIO = true;
	public static final boolean DEFAULT_SHOULD_USE_PPG = false;
	public static final boolean DEFAULT_EARLY_EXIT_DELETION = true;
	public static final boolean DEFAULT_ADVANCED_USER = false;
	public static final boolean DEFAULT_CHECK_SPACEANDBATTERY = true;
	public static final boolean DEFAULT_WRITE_LOG = false;

	// Keys for user preferences.
	public static final String PREF_NUMBER_RECORDINGS = "pref_numberRecordingsToKeep";
	public static final String PREF_MAC_ADDRESS = "pref_macAddress";
	public static final String PREF_GRAPH_SECONDS = "pref_graphSeconds";
	public static final String PREF_NOTIFICATIONS = "pref_notifications";
	public static final String PREF_RECORDING_START_DELAY = "pref_recordingstartdelay";
	public static final String PREF_RECORDING_DURATION = "pref_recordingduration";
	public static final String PREF_EARLY_EXIT_DELETION = "pref_earlyexitfiledeletion";
	public static final String PREF_ADVANCED_USER = "pref_advanced";
	public static final String PREF_CHECK_SPACEANDBATTERY = "pref_spaceandbattery";
	public static final String PREF_WRITE_LOG = "pref_writelog";
	public static final String PREF_ODI_THRESHOLD = "pref_odithreshold";
	public static final String PREF_FIRST_LAUNCH = "pref_firstlaunch";

	// File names.
	public static final String FILENAME_ORIENTATION = "orientation.dat";
	public static final String FILENAME_ACCELERATION_RAW = "acceleration_raw.dat";
	public static final String FILENAME_ACCELERATION_PROCESSED = "acceleration_processed.dat";
	public static final String FILENAME_AUDIO_RAW = "audio_raw.wav";
	public static final String FILENAME_AUDIO_PROCESSED = "audio_processed.dat";
	public static final String FILENAME_POSITION = "position.dat";
	public static final String FILENAME_PPG = "ppg.dat";
	public static final String FILENAME_SPO2 = "spo2.dat";
	public static final String FILENAME_LOG_DIRECTORY = "log";
	public static final String FILENAME_QUESTIONNAIRE = "Questionnaire.dat";
	public static final String FILENAME_ACCELERATION_MSE = "actigraphy_mse.dat";
	public static final String FILENAME_AUDIO_MSE = "audio_mse.dat";
	public static final String FILENAME_SVM_OUTPUT = "svm_output.dat";
	public static final String FILENAME_FEEDBACK_DIRECTORY = "feedback";
	
	// Keys for extras transferred between activities.
	public static final String EXTRA_ACTIGRAPHY_FILE = "actigraphyFile";
	public static final String EXTRA_AUDIO_FILE = "audioFile";
	public static final String EXTRA_DEMOGRAPHICS_FILE = "demographicsFile";
	public static final String EXTRA_PPG_FILE = "ppgFile";
	public static final String EXTRA_SPO2_FILE = "spo2File";
	public static final String EXTRA_RECORDING_DIRECTORY = "recordingDir";
	public static final String EXTRA_SCORE = "stopbangscore";
	public static final String EXTRA_RING_SETTING = "phoneRingSetting";
	public static final String EXTRA_COLLECT_ACTIGRAPHY = "useActigraphy";
	public static final String EXTRA_COLLECT_AUDIO = "useAudio";
	public static final String EXTRA_COLLECT_PPG = "usePpg";
	public static final String EXTRA_RECORDING_SETTINGS = "recordingSettings";
	public static final String EXTRA_QUESTION_NUMBER = "questionNumber";
	public static final String EXTRA_RECORDING_ID = "recordingId";

	// App parameters.
	public static final int PARAM_FLAGS_CHECK_PERIOD = 60; // Seconds
	public static final int PARAM_SAMPLERATE_ACCELEROMETER = 4; // Hz (approximate)
	public static final int PARAM_UPSAMPLERATE_ACCELEROMETER = 10; // Upsamples activity rate (smaller error)
	public static final int PARAM_SAMPLERATE_AUDIO = 45; // Hz (approximate)
	public static final int PARAM_SAMPLERATE_PPG = 75; // Hz
	public static final int PARAM_SAMPLERATE_SPO2 = 3; // Hz
	public static final int PARAM_ACCELEROMETER_RECORDING_PERIOD = 1000;
	public static final int PARAM_SWIPE_MIN_DISTANCE_DIP = 100;
	public static final int PARAM_SWIPE_MAX_OFFPATH_DIP = 200;
	public static final int PARAM_SWIPE_MIN_VELOCITY_DIP = 200;
	public static final long PARAM_UI_UPDATE_PERIOD = 1000; // Milliseconds
	public static final long PARAM_REQUIRED_SPACE = 536870912; // Bytes
	public static final float PARAM_REQUIRED_BATTERY = 0.6f; // proportion (1 is full)
	public static final float PARAM_BATTERY_NOTIFICATION_THRESHOLD = 0.05f;
	public static final float PARAM_GRAVITY_FILTER_COEFFICIENT = 0.6f;
	public static final float PARAM_ACTIVITY_GRAPH_MAX_Y = 5;
	public static final float PARAM_ACTIVITY_GRAPH_MIN_Y = -0.5f;
	public static final float PARAM_AUDIO_GRAPH_MAX_Y = 5000;
	public static final float PARAM_AUDIO_GRAPH_MIN_Y = -5000;
	public static final String PARAM_DATE_FORMAT = "yyyyMMddkkmmss";
	
	// Codes.
	public static final int CODE_BLUETOOTH_CONNECTION_SUCCESSFUL = 1;
	public static final int CODE_BLUETOOTH_CONNECTION_UNSUCCESSFUL = 2;
	public static final int CODE_BLUTOOTH_NO_PAIRED_DEVICES = 3;
	public static final int CODE_BLUETOOTH_PACKET_RECEIVED = 4;
	public static final int CODE_BLUETOOTH_REQUEST_ENABLE = 5;
	public static final int CODE_APP_NOTIFICATION_ID = 0;
	public static final int CODE_POSITION_SUPINE = 1;
	public static final int CODE_POSITION_PRONE = 2;
	public static final int CODE_POSITION_RIGHT = 4;
	public static final int CODE_POSITION_LEFT = 3;
	public static final int CODE_POSITION_SITTING = 5;
	public static final String CODE_APP_TAG = "SleepAp";

	// Facts of life...
	public static final int CONST_MILLIS_IN_MINUTE = 60000;
	public static final float CONST_DEGREES_PER_RADIAN = 57.2957795f;
	public static final String CONST_HTML_MIME_TYPE = "text/html";
	public static final String CONST_UTF8_ENCODING = "UTF-8";
}
