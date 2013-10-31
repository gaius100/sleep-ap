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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {
	public static List<Double> parseCsvFile(File csvFile, int index) throws IOException {
		String line;
		BufferedReader br;
		List<Double> dataOut = new ArrayList<Double>();
		int n = 0;
		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
			String[] token = line.split(",");
			if (token[index].equalsIgnoreCase("-Inf")) {
				dataOut.add(n, Double.NEGATIVE_INFINITY);
			} else {
				if (token[index].equalsIgnoreCase("Inf")) {
					dataOut.add(n, Double.POSITIVE_INFINITY);
				} else {
					dataOut.add(n, Double.parseDouble(token[index]));
					n++;
				}
			}
		}
		br.close();
		return dataOut;
	}

	/** Recursive directory delete. */
	public static boolean deleteDirectory(File parent) {
		if (parent == null) {
			return false;
		}
		boolean success = true;
		String[] filesInRecordingSession = parent.list();
		for (String file : filesInRecordingSession) {
			File childFile = new File(parent, file);
			if (childFile.isDirectory()) {
				success = deleteDirectory(childFile);
			} else {
				success = childFile.delete();
			}
			if (!success) {
				return false;
			}
		}
		// Then delete folder.
		return parent.delete();
	}

	public static int dipToPixels(int dipValue, Context context) {
		Resources r = context.getResources();
		float pixelsValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
		return (int) pixelsValue;
	}

	public static float odiCalculate(List<Double> spo2List, int threshold) {
		int spo2Length = spo2List.size();
		float calculatedOdi = 0;
		if (spo2Length > 120 * 3) {
			boolean desatSwitch = true;
			int desatLengthCounter = 0;
			List<Integer> odiIndices = new ArrayList<Integer>();
			for (int currentSpo2Ind = 1; currentSpo2Ind < spo2Length; currentSpo2Ind++) {
				// current Spo2 value to check
				double testVal = spo2List.get(currentSpo2Ind);
				// create window prior to current value 120 seconds prior to the
				// current point
				int avgWindowStartInd = currentSpo2Ind - 120 * 3;
				if (avgWindowStartInd < 0) {
					avgWindowStartInd = 0;
				}
				// do not include current point in mean calculation
				int avgWindowEndInd = currentSpo2Ind - 1;

				List<Double> avgWindow = spo2List.subList(avgWindowStartInd, avgWindowEndInd);
				double sumVal = 0;
				int valCount = 0;
				// calculate mean of window selected
				for (Double currValue : avgWindow) {
					if (!Double.isNaN(currValue)) {
						sumVal += currValue;
						valCount++;
					}
				}
				double avgForComparison = sumVal / valCount;
				// threshold is x% below the 120 second mean Spo2
				double currentThreshold = avgForComparison - threshold;

				if (!desatSwitch) {
					// if a desaturation has not previously been found

					if (testVal < currentThreshold) {
						// a desaturation below threshold is found- start
						// counting how long the desaturation has occured
						desatSwitch = true; // starts counter
						desatLengthCounter = 1;// reset counter
					}
				} else {
					// desaturation candidate is being counted
					if (testVal < currentThreshold) {
						// desaturation candidate event is still below
						// threshold- increment counter
						desatLengthCounter++;
					} else {
						// desaturation candidate is no longer below threshold-
						// check if it qualifies as a real desaturation and stop
						// counting desaturation length
						if (desatLengthCounter > 3 * 10) {
							// if the length of desaturation candidate event is
							// longer than 10 seconds, count it as a
							// desaturation for ODI
							odiIndices.add(currentSpo2Ind);
						}
						desatSwitch = false;
						// no longer a desaturation- stop counting
					}
				}

			}
			int odiCount = 0;
			if (!odiIndices.isEmpty()) {
				// how many desaturations were found during recording
				odiCount = odiIndices.size();
			}

			// number of desaturation events/ hour (3 samples per second, 3600
			// seconds per hour)}
			calculatedOdi = ((odiCount) / (spo2Length / 3.0f / 3600.0f));

			// return # of desaturations per hour
		} else {
			// if the recording is too short, return NaN;
			calculatedOdi = Float.NaN;
		}

		return calculatedOdi;
	}
}
