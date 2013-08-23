package ibme.sleepap.education;

import ibme.sleepap.Constants;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class OsaAnswers extends SleepApActivity {

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.osa_answers);

		TextView title = (TextView) findViewById(R.id.title);
		TextView answer = (TextView) findViewById(R.id.answer);

		final int[] titleIds = { R.string.infoquestion1, R.string.infoquestion2, R.string.infoquestion3, R.string.infoquestion4, R.string.infoquestion5,
				R.string.infoquestion6 };

		final int[] answerIds = { R.string.infoanswer1, R.string.infoanswer2, R.string.infoanswer3, R.string.infoanswer4, R.string.infoanswer5,
				R.string.infoanswer6 };

		Intent sendingIntent = getIntent();
		int questionNumber = sendingIntent.getIntExtra(Constants.EXTRA_QUESTION_NUMBER, 0);

		title.setText(getString(titleIds[questionNumber]));
		answer.setText(getString(answerIds[questionNumber]));

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

	private class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > Utils.dipToPixels(Constants.PARAM_SWIPE_MAX_OFFPATH_DIP, OsaAnswers.this))
					return false;
				// right to left swipe
				if (e2.getX() - e1.getX() > Utils.dipToPixels(Constants.PARAM_SWIPE_MIN_DISTANCE_DIP, OsaAnswers.this)
						&& Math.abs(velocityX) > Utils.dipToPixels(Constants.PARAM_SWIPE_MIN_VELOCITY_DIP, OsaAnswers.this)) {
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
