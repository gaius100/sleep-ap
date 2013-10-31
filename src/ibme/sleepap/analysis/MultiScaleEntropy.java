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

package ibme.sleepap.analysis;

public class MultiScaleEntropy {
	public static int i = 0;
	public static int M_MAX = 1000;
	static double SE;

	// We fix the set of parameters (m,r) that we wish to use
	static int lengthOfpattern=2; //length of the pattern (m)->corresponds to m in the C code?
	//static double r=0.25;
	
	static double standardDeviation(double[] u){
		double sum=0.0, sum2=0.0, sd;
		for (int j = 0; j < u.length; j++) {
	    sum += u[j];
	    sum2 += u[j] * u[j];
	    }
	    sd = Math.sqrt((sum2 - sum*sum/u.length)/(u.length - 1));
	    return (sd);
		 
	}
	
	 // y is sample entropy that has been coarse-grained
	static double SampleEntropy(double[] y, double r, double sd, int j, int m_max){
		
		int i, k, l, nlin_j;
	    double r_new;
	    int[] cont =new int[M_MAX];
	    int nlin = y.length;
	    // tell what values in y that we have to consider with respect to the scale
	    nlin_j = nlin - lengthOfpattern;
	    r_new = r*sd;  
		
	     for (i = 0; i < M_MAX; i++){
	     cont[i]=0;
	     }
	     for (i = 0; i < nlin_j; ++i) {
	    		for (l = i+1; l < nlin_j; ++l) { 
	    		    k = 0;
	    		    while (k < lengthOfpattern && Math.abs(y[i+k] - y[l+k]) <= r_new)
	    			cont[++k]++;
	    		    if (k == lengthOfpattern && Math.abs(y[i+lengthOfpattern] - y[l+lengthOfpattern]) <= r_new)
	    			cont[lengthOfpattern+1]++;
	    		} 
	     }     		    
      		
	     if (cont[lengthOfpattern+1] == 0 || cont[lengthOfpattern] == 0)
    		SE = -Math.log((double)1/((nlin_j)*(nlin_j-1)));
	     else
    		SE = -Math.log((double)cont[lengthOfpattern+1]/cont[lengthOfpattern]);

	     return SE;
    }
	
	public static String getCsvFormattedOutput(int[] scales, double[] sampleEntropy, long[] loadingTime){
		StringBuilder string2write;
		string2write = new StringBuilder();
		if (sampleEntropy.length == scales.length && sampleEntropy.length == loadingTime.length){
			for(int i = 0; i< scales.length; i++){
				string2write.append(scales[i]).append(",").append(sampleEntropy[i]).append(",").append(loadingTime[i]).append("\n");
			}
		}else{
			string2write.append("Error");
		}
		return string2write.toString();
	}
}
