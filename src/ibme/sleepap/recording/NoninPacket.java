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

/**
 * This packet class follows the specification for the Nonin WristOx2 3150,
 * using Serial Data Format #7. A packet consists of 25 frames, each of
 * which contains 5 bytes of data.
 * */
public class NoninPacket {
	
	public static final int PACKET_LENGTH_FRAMES = 25;
	public static final int FRAME_LENGTH_BYTES = 5;
	private static final int STATUS_BYTE_INDEX = 0;
	private static final int PPG_MSB_BYTE_INDEX = 1;
	private static final int PPG_LSB_BYTE_INDEX = 2;
	private static final int FLAGS_BYTE_INDEX = 3;
	private static final int CHECKSUM_BYTE_INDEX = 4;

	private int[][] completeNoninPacket = new int[PACKET_LENGTH_FRAMES][FRAME_LENGTH_BYTES];
	private int iCurrentFrame;
	private double[] ppgVals;

	public NoninPacket() {
		iCurrentFrame = 0;
	}

	public void clearPacket() {
		for (int iFrame = 0; iFrame < PACKET_LENGTH_FRAMES; iFrame++) {
			for (int iByte = 0; iByte < FRAME_LENGTH_BYTES; iByte++) {
				completeNoninPacket[iFrame][iByte] = 0;
			}
		}
		iCurrentFrame = 0;
	}

	public void addFrame(byte[] frameBuffer) {
		for (int i = 0; i < FRAME_LENGTH_BYTES; i++) {
			completeNoninPacket[iCurrentFrame][i] = frameBuffer[i];
		}
		iCurrentFrame++;
	}

	public boolean isFull() {
		return (iCurrentFrame == PACKET_LENGTH_FRAMES);
	}

	public double[] getPpgVals() {
		ppgVals = new double[PACKET_LENGTH_FRAMES];
		for (int i = 0; i < PACKET_LENGTH_FRAMES; i++) {
			ppgVals[i] = (((completeNoninPacket[i][PPG_MSB_BYTE_INDEX] & 0xFF) << 7) + (completeNoninPacket[i][PPG_LSB_BYTE_INDEX] & 0xFF));
		}
		return ppgVals;
	}

	public double getSpo2() {
		return completeNoninPacket[2][3]; // 4 beat SpO2 average
	}

	public static boolean isValidFrame(byte[] frame) {
		int statusByte = frame[STATUS_BYTE_INDEX] & 0xFF;
		int ppgMsbByte = frame[PPG_MSB_BYTE_INDEX] & 0xFF;
		int ppgLsbByte = frame[PPG_LSB_BYTE_INDEX] & 0xFF;
		int flagsByte = frame[FLAGS_BYTE_INDEX] & 0xFF;
		int checksumByte = frame[CHECKSUM_BYTE_INDEX] & 0xFF;
		if ((statusByte & 0x80) == 0) {
			// First bit of 1st byte should be 1.
			return false;
		}
		// Checksum.
		int byteSum = (statusByte + ppgMsbByte + ppgLsbByte + flagsByte) % 256;
		int[] reformattedInput = new int[5];
		reformattedInput[0] = statusByte;
		reformattedInput[1] = ppgMsbByte;
		reformattedInput[2] = ppgLsbByte;
		reformattedInput[3] = flagsByte;
		reformattedInput[4] = checksumByte;
		return (byteSum == checksumByte);
	}

	public static boolean isSyncFrame(byte[] frame) {
		// Bit 0 of statusByte is 1 for first frame of the packet.
		int statusByte = frame[STATUS_BYTE_INDEX] & 0xFF;
		return ((statusByte & 0x01) == 1);
	}
}
