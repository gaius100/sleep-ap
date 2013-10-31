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

package ibme.sleepap.history;

import ibme.sleepap.Constants;
import ibme.sleepap.MainMenu;
import ibme.sleepap.R;
import ibme.sleepap.SleepApActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ViewHistory extends SleepApActivity {

	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_history);

		dbHelper = new DatabaseHelper(this);
		database = dbHelper.getWritableDatabase();

		Cursor cursor = database.query(HistoryTable.TABLE_NAME, HistoryTable.ALL_COLUMNS, null, null, null, null, HistoryTable.COLUMN_START_DATE);
		String[] fromColumns = { HistoryTable.COLUMN_START_DATE_DISPLAY, HistoryTable.COLUMN_ODI, HistoryTable.COLUMN_QUESTIONNAIRE,
				HistoryTable.COLUMN_SVM_RESULT };
		int[] toViews = { R.id.date, R.id.odi, R.id.questionnaire, R.id.svm };

		if (cursor.getCount() > 0) {
			findViewById(R.id.noHistoryFound).setVisibility(View.GONE);

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.history_item, cursor, fromColumns, toViews, 0);

			adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
				@Override
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					TextView textView = (TextView) view;
					String name = cursor.getColumnName(columnIndex);
					if (name.equals(HistoryTable.COLUMN_ODI)) {
						String odi = cursor.getString(columnIndex);
						textView.setText(odi);
						if (odi.equals("-")) {
							return true;
						}
						float odival = Float.parseFloat(odi);
						int color;
						if (odival < 5) {
							color = 0xFF00FF00;
						} else {
							color = 0xFFFF0000;
						}
						textView.setTextColor(color);
						return true;
					}
					if (name.equals(HistoryTable.COLUMN_QUESTIONNAIRE)) {
						String stopBangScore = cursor.getString(columnIndex);
						textView.setText(stopBangScore);
						if (stopBangScore.equals("-")) {
							return true;
						}
						int stopBangScoreVal = Integer.parseInt(stopBangScore);
						int color;
						if (stopBangScoreVal < 3) {
							color = 0xFF00FF00;
						} else {
							color = 0xFFFF0000;
						}
						textView.setTextColor(color);
						return true;
					}
					if (name.equals(HistoryTable.COLUMN_SVM_RESULT)) {
						String svmScore = cursor.getString(columnIndex);
						if (svmScore.equals("-")) {
							textView.setText(svmScore);
							return true;
						}
						Float stopBangScoreVal = Float.parseFloat(svmScore);
						textView.setText(String.format("%.0f%%", stopBangScoreVal*100));
						int color;
						if (stopBangScoreVal < 0.5) {
							color = 0xFF00FF00;
						} else {
							color = 0xFFFF0000;
						}
						textView.setTextColor(color);
						return true;
					}					
					return false;
				}
			});

			ListView listView = (ListView) findViewById(R.id.historyList);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(ViewHistory.this, ViewAnalysis.class);
					intent.putExtra(Constants.EXTRA_RECORDING_ID, id);
					startActivity(intent);
					overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);
				}
			});
		} else {
			findViewById(R.id.columnHeadings).setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		database.close();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(ViewHistory.this, MainMenu.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}