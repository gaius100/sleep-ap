package ibme.sleepap.history;

import ibme.sleepap.Constants;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import ibme.sleepap.analysis.Thermometer;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewAnalysis extends SleepApActivity {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase datasource;
	private TextView questionnaireScore, svmScore, odiScore, cloudScore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.perform_analysis);

		dbHelper = new DatabaseHelper(this);
		datasource = dbHelper.getWritableDatabase();

		Intent sendingIntent = getIntent();
		long id = sendingIntent.getLongExtra(Constants.EXTRA_RECORDING_ID, -1);

		/** Set up score TextViews. */
		questionnaireScore = (TextView) findViewById(R.id.stopBang);
		svmScore = (TextView) findViewById(R.id.svmOutput);
		odiScore = (TextView) findViewById(R.id.odi);
		cloudScore = (TextView) findViewById(R.id.cloudOutput);

		/** Query database to see if this recording has been analysed before. */
		Cursor cursor = datasource.query(HistoryTable.TABLE_NAME, HistoryTable.ALL_COLUMNS, HistoryTable.COLUMN_ID + " = " + id, null, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			String cloudScoreString = cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.COLUMN_CLOUD_RESULT));
			String svmScoreString = cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.COLUMN_SVM_RESULT));
			String questionnaireScoreString = cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.COLUMN_QUESTIONNAIRE));
			String odiScoreString = cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.COLUMN_ODI));
			
			cloudScore.setText(getString(R.string.cloudDefaultScore) + " " + cloudScoreString);
			svmScore.setText(getString(R.string.svmDefaultScore) + " " + svmScoreString);
			questionnaireScore.setText(getString(R.string.stopBangDefaultScore) + " " + questionnaireScoreString);
			odiScore.setText(getString(R.string.odiDefaultScore) + " " + odiScoreString);
			
			if (cloudScoreString != null && cloudScoreString != "" && cloudScoreString != "-") {
				findViewById(R.id.cloudOutputContainer).setVisibility(View.VISIBLE);
			}
			if (svmScoreString != null && svmScoreString != "" && svmScoreString != "-") {
				findViewById(R.id.svmOutputContainer).setVisibility(View.VISIBLE);
			}
			if (questionnaireScoreString != null && questionnaireScoreString != "" && questionnaireScoreString != "-") {
				findViewById(R.id.stopBangContainer).setVisibility(View.VISIBLE);
			}
			if (odiScoreString != null && odiScoreString != "" && odiScoreString != "-") {
				findViewById(R.id.odiContainer).setVisibility(View.VISIBLE);
			}
			
			// Update thermometer
			ImageView thermometerImage = (ImageView) findViewById(R.id.thermometer);
			// Get mutable bitmap from drawable to copy onto canvas
			Bitmap mutableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thermometer).copy(Bitmap.Config.ARGB_8888, true);
			Canvas canvas = new Canvas(mutableBitmap);
			// Draw thermometer onto canvas, then draw 'temperature' onto it.
			canvas.drawBitmap(mutableBitmap, 0, 0, null);
			Thermometer thermometer = new Thermometer(ViewAnalysis.this, canvas);
			thermometer.drawThermometer(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.COLUMN_SVM_RESULT))));
			thermometerImage.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));
			
			TextView overallRisk = (TextView) findViewById(R.id.overallRisk);
			if (Double.parseDouble(svmScoreString) > 0.5) {
				overallRisk.setText(getString(R.string.atRisk));
				overallRisk.setTextColor(getResources().getColor(R.color.darkred));
			} else {
				overallRisk.setText(getString(R.string.notAtRisk));
				overallRisk.setTextColor(getResources().getColor(R.color.darkgreen));
			}

			((Button) findViewById(R.id.cloudButton)).setEnabled(false);
		} else {
			// TODO: handle
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
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.enteringfromleft, R.anim.exitingtoright);
	}
}
