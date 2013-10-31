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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.preference.PreferenceManager;
import android.util.Log;

//Class from http://i-liger.com/article/android-wav-audio-recording which is itself adapted from Rehearsal Assistant
public class ExtAudioRecorder {

	private final static int SAMPLE_RATE = 8000;
	private File audioProcessedFile;
	private boolean shouldWriteToFile;
	private int TIMER_INTERVAL = 50; //Interval of time to update the buffer, write it to file and add a sample to the audio signal graph
	//This might be changed if the buffer size created by this value is smaller than the system's minimum buffer size
	private int processingSignalInterval; //Number of buffer reading/writing iterations before performing the signal processing step (this is equivalent to 1 second) 
	private int processingSignalIterationCounter = 0; //Counter of the buffer reading/writing iterations
	private int processingSignalCounter = 0; //Counter of the total number of audio samples in one group of signals to be processed together
	private double sumAudio = 0; //Sum of the audio samples (required to compute the variance)
	private double sumAudioSquare = 0; //Sum of the square of the audio samples (required to compute the variance)
	private SharedPreferences sharedPreferences;
	private Queue<Double> audioQueue;
	private AudioRecord audioRecord;
	private String outputFilePath;
	private State state;
	private RandomAccessFile randomAccessWriter;
	private short nChannels;
	private int sampleRate;
	private short sampleSizeBits;
	private int bufferSize;
	private int audioSource;
	private int audioFormat;
	private int nFramesPerOutput;
	private int nBytesAfterHeader;
	private Thread recordingThread = null;


	public enum State {
		INITIALIZING, READY, RECORDING, ERROR, STOPPED
	};

	public void setAudioProcessedFile(File file) {
		audioProcessedFile = file;
	}

	public Queue<Double> getAudioQueue() {
		return audioQueue;
	}

	public boolean getShouldWrite() {
		return shouldWriteToFile;
	}

	public void setShouldWrite(boolean shouldWrite) {
		this.shouldWriteToFile = shouldWrite;
	}


	private void writeAudioDataToFile(){

		// Fill buffer.

		try {
			byte data[] = new byte[bufferSize];
			BufferedWriter out = new BufferedWriter(new PrintWriter(audioProcessedFile));



			while(state==State.RECORDING)
			{

				audioRecord.read(data, 0, bufferSize);																	
				int bufferLength = bufferSize;


				// Write buffer to file.
				randomAccessWriter.write(data);
				nBytesAfterHeader += bufferLength;
				bufferLength = bufferLength / 2;
				short curSample = 0;
				for (int i = 0; i < bufferLength; i++) {
					ByteBuffer bb = ByteBuffer.allocate(2);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.put(data[i * 2]);
					bb.put(data[i * 2 + 1]);
					curSample = bb.getShort(0);
					sumAudio += curSample;
					sumAudioSquare += curSample * curSample;
				}

				audioQueue.add((double) curSample);
				int secsToDisplay = Integer.parseInt(sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS,
						Constants.DEFAULT_GRAPH_RANGE));
				int numberExtraAudioSamples = audioQueue.size() - secsToDisplay * 1000 / TIMER_INTERVAL;
				if (numberExtraAudioSamples > 0) {
					for (int i = 0; i < numberExtraAudioSamples; i++) {
						audioQueue.remove();
					}
				}

				processingSignalCounter += bufferLength;
				processingSignalIterationCounter += 1;

				if (processingSignalIterationCounter == processingSignalInterval) {
					processingSignalIterationCounter = 0;
					double meanAudio = sumAudio / (processingSignalCounter);
					double log_variance = Math.log(1 + sumAudioSquare / processingSignalCounter - meanAudio * meanAudio);

					if (shouldWriteToFile) {
						out.write(String.valueOf(log_variance) + "\n");
						out.flush();
					}

					sumAudio = 0;
					sumAudioSquare = 0;
					processingSignalCounter = 0;
					processingSignalIterationCounter = 0;
				}

			}
			out.close();

		}catch (IOException e) {
			Log.e(ExtAudioRecorder.class.getName(), "Error occured in updateListener, recording is aborted");
		}



	}

	/**
	 * Default constructor: Instantiates a new recorder, in case of compressed
	 * recording the parameters can be left as 0. In case of errors, no
	 * exception is thrown, but the state is set to ERROR.
	 */
	public ExtAudioRecorder(Context context) {

		audioQueue = new LinkedList<Double>();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			sampleSizeBits = 16;
			nChannels = 1;
			audioSource = AudioSource.MIC;
			sampleRate = SAMPLE_RATE;
			audioFormat = AudioFormat.ENCODING_PCM_16BIT;
			nFramesPerOutput = SAMPLE_RATE * TIMER_INTERVAL / 1000;
			bufferSize = nFramesPerOutput * sampleSizeBits * nChannels / 8;

			int minBufferSize =AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT); 
			if (bufferSize < minBufferSize) {
				bufferSize = minBufferSize;
				// Set frame period and timer interval accordingly.
				nFramesPerOutput = bufferSize / (sampleSizeBits * nChannels / 8);

				TIMER_INTERVAL = (nFramesPerOutput*1000)/SAMPLE_RATE;
			}
			processingSignalInterval = 1000 / TIMER_INTERVAL;


			audioRecord = new AudioRecord(AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

			if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
				throw new Exception("AudioRecord initialization failed");
			}

			//audioRecord.setRecordPositionUpdateListener(updateListener);
			//audioRecord.setPositionNotificationPeriod(nFramesPerOutput);
			this.state = State.INITIALIZING;

		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG, "Error while initializing ExtAudioRecorder", e);
			state = State.ERROR;
		}
	}

	public void setOutputFile(File file) {
		try {
			if (state == State.INITIALIZING) {
				outputFilePath = file.getPath();
			}
		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG, "Error setting output file" , e);
			state = State.ERROR;
		}
	}

	public void prepare() {
		try {
			if (state == State.INITIALIZING) {
				if ((audioRecord.getState() == AudioRecord.STATE_INITIALIZED) & (outputFilePath != null)) {
					// write file header
					randomAccessWriter = new RandomAccessFile(outputFilePath, "rw");
					// Set file length to 0 to prevent unexpected behaviour in
					// case the file already existed.
					randomAccessWriter.setLength(0); 
					randomAccessWriter.writeBytes("RIFF");
					// Final file size not yet known.
					randomAccessWriter.writeInt(0);
					randomAccessWriter.writeBytes("WAVE");
					randomAccessWriter.writeBytes("fmt ");
					// Sub-chunk size, 16 for PCM.
					randomAccessWriter.writeInt(Integer.reverseBytes(16));
					// AudioFormat, 1 for PCM.
					randomAccessWriter.writeShort(Short.reverseBytes((short) 1));
					// Number of channels, 1 for mono and 2 for stereo.
					randomAccessWriter.writeShort(Short.reverseBytes(nChannels));
					// Sample rate.
					randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate));
					// Byte rate, SampleRate*numberOfChannels*BitsPerSample/8
					randomAccessWriter.writeInt(Integer.reverseBytes(sampleRate * sampleSizeBits * nChannels / 8));
					// Block align, NumberOfChannels*BitsPerSample/8.
					randomAccessWriter.writeShort(Short.reverseBytes((short) (nChannels * sampleSizeBits / 8)));
					// Bits per sample.
					randomAccessWriter.writeShort(Short.reverseBytes(sampleSizeBits));
					randomAccessWriter.writeBytes("data");
					// Date chunk size not known yet, write 0.
					randomAccessWriter.writeInt(0);
					state = State.READY;
				} else {
					Log.e(Constants.CODE_APP_TAG, "AudioRecord not initialised during prepare()");
					state = State.ERROR;
				}
			} else {
				Log.e(Constants.CODE_APP_TAG, "ExtAudioRecorder not initialised during prepare()");
				release();
				state = State.ERROR;
			}
		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG, "Error preparing ExtAudioRecorder, e");
			state = State.ERROR;
		}
	}

	/**
	 * Releases the resources associated with this class, and removes the
	 * unnecessary files, when necessary.
	 */
	public void release() {
		if (state == State.RECORDING) {
			stop();
		} else {
			if ((state == State.READY)) {
				try {
					randomAccessWriter.close(); // Remove prepared file
				} catch (IOException e) {
					Log.e(Constants.CODE_APP_TAG, "IOException while releasing FileWriter", e);
				}
				(new File(outputFilePath)).delete();
			}
		}
		if (audioRecord != null) {
			audioRecord.release();
		}
	}

	/**
	 * Resets the recorder to the INITIALIZING state, as if it was just created.
	 * In case the class was in RECORDING state, the recording is stopped. In
	 * case of exceptions the class is set to the ERROR state.
	 */
	public void reset() {
		try {
			if (state != State.ERROR) {
				release();
				outputFilePath = null;
				audioRecord = new AudioRecord(audioSource, sampleRate, nChannels + 1, audioFormat, bufferSize);
				state = State.INITIALIZING;
			}
		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG, "Error resetting ExtAudioRecorder", e);
			state = State.ERROR;
		}
	}

	/**
	 * Starts the recording, and sets the state to RECORDING. Call after
	 * prepare().
	 */
	public void start() {
		if (state == State.READY) {
			nBytesAfterHeader = 0;
			audioRecord.startRecording();
			state = State.RECORDING;

			
			//Creates a thread that will record and process the audio data
			recordingThread = new Thread(new Runnable() {

				@Override
				public void run() {
					writeAudioDataToFile();
				}
			},"AudioRecorder Thread");

			recordingThread.start();


		} else {
			Log.e(Constants.CODE_APP_TAG, "AudioRecord not ready when starting");
			state = State.ERROR;
		}
	}





	/**
	 * Stops the recording, and sets the state to STOPPED. In case of further
	 * usage, a reset is needed. Also finalizes the wave file in case of
	 * uncompressed recording.
	 */
	public void stop() {
		if (state == State.RECORDING) {
			audioRecord.stop();
			try {
				// Write size to RIFF header.
				randomAccessWriter.seek(4); 
				randomAccessWriter.writeInt(Integer.reverseBytes(36 + nBytesAfterHeader));
				// Write size to Subchunk2Size field.
				randomAccessWriter.seek(40);
				randomAccessWriter.writeInt(Integer.reverseBytes(nBytesAfterHeader));
				randomAccessWriter.close();
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG, "IOException closing output file during stop()", e);
				state = State.ERROR;
			}
			state = State.STOPPED;
		} else {
			Log.e(Constants.CODE_APP_TAG, "ExtAudioRecorder not recording when stop() is called");
			state = State.ERROR;
		}
	}
}
