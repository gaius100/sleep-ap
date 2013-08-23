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

package ibme.sleepap.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DemographicsContainer {
	private File demographicsFile;
	private List<String> readData;

	private boolean loudSnorer;
	private boolean tired;
	private boolean stopBreathing;
	private boolean hypertensive;
	private double bmi;
	private int age;
	private double neckSize;
	private Gender gender;
	private String ethnicity;
	private double height;
	private double weight;
	private int score;

	private enum Gender {
		Male, Female, None
	}

	public DemographicsContainer(File demographicsFile) {
		this.demographicsFile = demographicsFile;
		loadDemographics(demographicsFile);
	}

	private void loadDemographics(File inputfile) {
		String line;
		BufferedReader br;
		readData = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(inputfile));
			while ((line = br.readLine()) != null) {
				readData.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!readData.isEmpty()) {
			this.loudSnorer = Integer.parseInt(readData.get(0)) == 1;
			this.tired = Integer.parseInt(readData.get(1)) == 1;
			this.stopBreathing = Integer.parseInt(readData.get(2)) == 1;
			this.hypertensive = Integer.parseInt(readData.get(3)) == 1;
			this.bmi = Double.parseDouble(readData.get(4));
			this.age = Integer.parseInt(readData.get(5));
			this.neckSize = Double.parseDouble(readData.get(6));
			switch (Integer.parseInt(readData.get(7))) {
			case 1:
				this.gender = Gender.Male;
				break;
			case 0:
			default:
				this.gender = Gender.Female;
				break;
			}
			this.ethnicity = readData.get(8);
			this.height = Double.parseDouble(readData.get(9));
			this.weight = Double.parseDouble(readData.get(10));
			this.score = Integer.parseInt(readData.get(11));
		} else {
			this.loudSnorer = false;
			this.tired = false;
			this.stopBreathing = false;
			this.hypertensive = false;
			this.bmi = Double.NaN;
			this.age = -1;
			this.neckSize = Double.NaN;
			this.gender = Gender.None;
			this.ethnicity = "Unknown";
			this.height = Double.NaN;
			this.weight = Double.NaN;
			this.score = -1;
		}
	}

	public File getDemographicsFile() {
		return demographicsFile;
	}

	public String getGender() {
		return gender.toString();
	}

	public int getAge() {
		return age;
	}

	public double getWeight() {
		return weight;
	}

	public double getNeckSize() {
		return neckSize;
	}

	public double getBmi() {
		return bmi;
	}

	public double getHeight() {
		return height;
	}

	public boolean isLoudSnorer() {
		return loudSnorer;
	}

	public boolean isTired() {
		return tired;
	}

	public boolean isStopBreathing() {
		return stopBreathing;
	}

	public boolean isHypertensive() {
		return hypertensive;
	}

	public String getEthnicity() {
		return ethnicity;
	}
	
	public int getScore() {
		return score;
	}
}
