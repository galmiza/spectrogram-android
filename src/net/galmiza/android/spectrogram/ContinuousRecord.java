package net.galmiza.android.spectrogram;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public class ContinuousRecord {
	
	// Attributes
	private AudioRecord audioRecord;
	private int samplingRate;
	private int recordLength;
	private Thread thread;
	private boolean run;
	
	/**
	 * Constructor
	 */
	ContinuousRecord(int samplingRate) {
		this.samplingRate = samplingRate;
		run = false;
	}
	
	int getBufferLength() {
		return recordLength;
	}
	
	/**
	 * Prepare recording service
	 * The buffer size can be forced to be multiple of the param (size in sample count)
	 * Param multiple is ineffective if set to 1
	 */
	public void prepare(int multiple) {
		
		// Setup buffer size
		int BYTES_PER_SHORT = 2;
	    recordLength = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)/BYTES_PER_SHORT;
	    
	    // Increase buffer size so that it is a multiple of the param
	    int r = recordLength % multiple;
	    if (r>0) recordLength += (multiple-r);
	    
	    // Log value
		//Log.d("ContinuousRecord","Buffer size = "+recordLength+" samples");
	    
	    // Init audio recording from MIC
	    audioRecord = new AudioRecord(AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recordLength*BYTES_PER_SHORT);
	}
	
	// Listener
	public static interface OnBufferReadyListener {
        void onBufferReady(short[] buffer);
    }
	
	// Start recording
	public void start(final OnBufferReadyListener listener) {
		if (!run && audioRecord!=null) {
			run = true;
			//Log.d("ContinuousRecord","Starting service...");
			audioRecord.startRecording(); 
	       	final short[] recordBuffer = new short[recordLength];
	
	       	thread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (run) {
				       	audioRecord.read(recordBuffer, 0, recordLength);
				       	listener.onBufferReady(recordBuffer);
					}
				}
			});
	       	thread.start();
			//Log.d("ContinuousRecord","Service started");
		}
	}
	
	// Stop recording
	public void stop() {
		if (run && audioRecord!=null) {
			//Log.d("ContinuousRecord","Stopping service...");
			run = false;
			while (thread.isAlive())
				try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
			audioRecord.stop();
			//Log.d("ContinuousRecord","Service stopped");
		}
	}
	
	// Leave recording service
	public void release() {
		if (!run && audioRecord!=null)
			audioRecord.release();
   	}
}
