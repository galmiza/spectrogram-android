/**
 * Spectrogram Android application
 * Copyright (c) 2013 Guillaume Adam  http://www.galmiza.net/

 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely,
 * subject to the following restrictions:

 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package net.galmiza.android.engine.sound;

/**
 * SoundEngine behaves as the interface to the native functions
 * These native functions are implemented in C
 */
public class SoundEngine {
	static {
		System.loadLibrary("sound-engine");
	}
	
	// Core function
	public native void fft(float[] re, float[] im, int N, int inverse );
	public native void initFSin();

	// Conversion
	public native void toPolar(float[] re, float[] im, int N );
	public native void toCartesian(float[] re, float[] im, int N );

	// Data management
	public native void shortToFloat(short[] s, float[] f, int N );
	public native void floatToShort(float[] f, short[] s, int N );
	public native void clearShort(short[] s, int N );
	public native void clearFloat(float[] f, int N );

	// Data analysis
	public native void getMeanFrequency(float[] io, float[] m, int N );

	// Windows functions
	public native void window(float[] f, int N );
	public native void windowRectangular(float[] f, int N );
	public native void windowTriangular(float[] f, int N );
	public native void windowWelch(float[] f, int N );
	public native void windowHanning(float[] f, int N );
	public native void windowHamming(float[] f, int N );
	public native void windowBlackman(float[] f, int N );
	public native void windowNuttall(float[] f, int N );
	public native void windowBlackmanNuttall(float[] f, int N );
	public native void windowBlackmanHarris(float[] f, int N );	
}
