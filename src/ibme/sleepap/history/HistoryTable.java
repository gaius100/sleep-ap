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

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HistoryTable {

	// Database table.
	public final static String TABLE_NAME = "history";
	public final static String COLUMN_ID = "_id";
	public final static String COLUMN_START_DATE = "start_date";
	public final static String COLUMN_START_DATE_DISPLAY = "start_date_display";
	public final static String COLUMN_ODI = "odi";
	public final static String COLUMN_QUESTIONNAIRE = "questionaire";
	public final static String COLUMN_SVM_RESULT = "svm";
	public final static String COLUMN_CLOUD_GUID = "cloud_guid";
	public final static String COLUMN_CLOUD_RESULT = "cloud";

	public final static String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_START_DATE,
			COLUMN_START_DATE_DISPLAY, COLUMN_ODI, COLUMN_QUESTIONNAIRE,
			COLUMN_SVM_RESULT, COLUMN_CLOUD_GUID, COLUMN_CLOUD_RESULT };

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_START_DATE + " text, " + COLUMN_START_DATE_DISPLAY
			+ " text, " + COLUMN_ODI + " text, " + COLUMN_QUESTIONNAIRE
			+ " text, " + COLUMN_SVM_RESULT + " text, " + COLUMN_CLOUD_GUID
			+ " text, " + COLUMN_CLOUD_RESULT + " text" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(HistoryTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion);

		/*
		 * switch (oldVersion) { case 1: Log.w(HistoryTable.class.getName(),
		 * "1. Renaming column openflightsId to cloud_guid in history");
		 * 
		 * database.execSQL("ALTER TABLE history RENAME TO history_old");
		 * database.execSQL("CREATE TABLE history " + "(" +
		 * "    start_date text primary key, " + "    odi text, " +
		 * "    questionaire text, " + "    svm text, " +
		 * "    cloud_guid text, " + "    cloud text " + ")"); database.execSQL(
		 * "INSERT INTO history(start_date, odi, questionaire, svm, cloud_guid, cloud) "
		 * + "SELECT start_date, odi, questionaire, svm, openflightsId, cloud "
		 * + "FROM history_old"); database.execSQL("DROP TABLE history_old"); }
		 */
	}
}
