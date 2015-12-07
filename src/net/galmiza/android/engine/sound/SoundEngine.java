package net.galmiza.android.engine.sound;

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
