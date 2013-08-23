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

package ibme.sleepap.education;

import ibme.sleepap.Constants;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.Utils;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

public class AudioSamples extends SleepApActivity {
	private MediaPlayer mediaPlayer;
	private int lastPlayedResource;
	private ProgressBar progressBar;
	private Button[] playButtons = new Button[3];
	private ProgressUpdater progressUpdater;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	private class ProgressUpdater extends AsyncTask<Void, Integer, Void> {
		private int _index;
		private int _duration;

		public ProgressUpdater(int index, int duration) {
			_index = index;
			_duration = duration;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				while (mediaPlayer.getCurrentPosition() < _duration) {
					Thread.sleep(500);
					publishProgress(mediaPlayer.getCurrentPosition());
				}
			} catch (Exception e) {
				Log.d(Constants.CODE_APP_TAG, "Thread sleep interruption", e);
				return null;
			}
			progressBar.setProgress(progressBar.getMax());
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress(values[0]);
			super.onProgressUpdate(values);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Void result) {
			playButtons[_index].setBackgroundDrawable((getResources().getDrawable(R.drawable.avplay)));
			super.onPostExecute(result);
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.audio_samples);

		lastPlayedResource = 0;
		progressBar = (ProgressBar) findViewById(R.id.audioProgress);

		playButtons[0] = (Button) findViewById(R.id.playNormal);
		playButtons[1] = (Button) findViewById(R.id.playSnorer);
		playButtons[2] = (Button) findViewById(R.id.playOsa);
		final int[] soundClipIds = { R.raw.normal, R.raw.snorer, R.raw.apnoeic };

		for (int i = 0; i < 3; i++) {
			final int index = i; // Needs to be final so OnClickListener can use
									// it.
			playButtons[i].setOnClickListener(new OnClickListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onClick(View v) {
					for (int ii = 0; ii < 3; ii++) {
						if (ii != index) {
							playButtons[ii].setBackgroundDrawable((getResources().getDrawable(R.drawable.avplay)));
						}
					}
					if (mediaPlayer != null && mediaPlayer.isPlaying() && lastPlayedResource == soundClipIds[index]) {
						// This audio is currently playing, and should be
						// stopped.
						progressUpdater.cancel(true);
						mediaPlayer.stop();
						mediaPlayer = null;
						playButtons[index].setBackgroundDrawable((getResources().getDrawable(R.drawable.avplay)));
						progressBar.setProgress(0);

					} else {
						stopSound();
						mediaPlayer = MediaPlayer.create(getApplicationContext(), soundClipIds[index]);
						int duration = mediaPlayer.getDuration();
						mediaPlayer.start();
						lastPlayedResource = soundClipIds[index];
						playButtons[index].setBackgroundDrawable((getResources().getDrawable(R.drawable.avstop)));
						progressBar.setMax(duration);
						progressBar.setProgress(0);
						progressUpdater = new ProgressUpdater(index, duration);
						progressUpdater.execute();
					}
				}
			});
		}

		// Gesture detection
		gestureDetector = new GestureDetector(this, new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		// Do this for each view added to the grid
		findViewById(R.id.entire_view).setOnTouchListener(gestureListener);
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.enteringfromleft, R.anim.exitingtoright);
	}

	protected void stopSound() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer = null;
		}
	}

	private class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > Utils.dipToPixels(Constants.PARAM_SWIPE_MAX_OFFPATH_DIP, AudioSamples.this))
					return false;
				// right to left swipe
				if (e2.getX() - e1.getX() > Utils.dipToPixels(Constants.PARAM_SWIPE_MIN_DISTANCE_DIP, AudioSamples.this)
						&& Math.abs(velocityX) > Utils.dipToPixels(Constants.PARAM_SWIPE_MIN_VELOCITY_DIP, AudioSamples.this)) {
					finish();
					overridePendingTransition(R.anim.enteringfromleft, R.anim.exitingtoright);
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}
}
