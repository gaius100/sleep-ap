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
import ibme.sleepap.MainMenu;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.Utils;
import ibme.sleepap.recording.ChooseSignals;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StopBangResults extends SleepApActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_bang_results);

		TextView advice = (TextView) findViewById(R.id.advice);
		Button mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
		Button recordButton = (Button) findViewById(R.id.recordButton);

		mainMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StopBangResults.this, MainMenu.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		recordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StopBangResults.this, ChooseSignals.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

		// Get score from the questionnaire and set the text accordingly.
		Intent sendingIntent = getIntent();
		final int score = sendingIntent.getIntExtra(Constants.EXTRA_SCORE, -1);
		if (score == -1) {
			advice.setText(getText(R.string.scoreError));
		} else {
			if (score >= 3) {
				advice.setText(getText(R.string.scoreHigh));
				// TODO: refer to nearest sleep clinic.
			} else {
				advice.setText(getText(R.string.scoreLow));
			}
		}

		final ImageView severityBar = (ImageView) findViewById(R.id.severitybar);
		final ImageView picker = (ImageView) findViewById(R.id.picker);

		picker.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				int pickerWidthDip = 20;
				int widthPx = severityBar.getWidth();
				float marginLeft = (widthPx - Utils.dipToPixels(pickerWidthDip, getApplicationContext())) * (((float)score) / 8);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dipToPixels(pickerWidthDip, getApplicationContext()),
						LayoutParams.WRAP_CONTENT);
				lp.setMargins((int) marginLeft, 0, 0, 0);
				picker.setLayoutParams(lp);
				return true;
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.enteringfromleft, R.anim.exitingtoright);
	}
}