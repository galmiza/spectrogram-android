#include <stdlib.h>
#include <jni.h>

/*
 * Bridge between C and java
 * WARNING FLOAT FAILS TO BE PASSED AS PARAMTERS
 */

// Prototypes
///////////////
void window_rectangular(float* fr, unsigned int n);
void window_triangular(float* fr, unsigned int n);
void window_welch(float* fr, unsigned int n);
void window_blackman(float* fr, unsigned int n);
void window_nuttall(float* fr, unsigned int n);
void window_blackman_nuttall(float* fr, unsigned int n);
void window_blackman_harris(float* fr, unsigned int n);
void window_hanning(float* fr, unsigned int n);
void window_hamming(float* fr, unsigned int n);


// Core functions
///////////////////
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_fft
	(JNIEnv* env, jobject obj, jfloatArray jre, jfloatArray jim, jint N, jint inverse ) {

	float* rre = (float*) (*env)->GetPrimitiveArrayCritical(env, jre, 0);
	float* rim = (float*) (*env)->GetPrimitiveArrayCritical(env, jim, 0);
	fft(rre, rim, N, inverse);
	(*env)->ReleasePrimitiveArrayCritical(env, jre, rre, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, jim, rim, 0);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_initFSin
	(JNIEnv* env, jobject obj ) {

	init_fsin();
}


// Windows functions export
////////////////////////////
typedef void (*func)(jfloatArray jf, unsigned int n);
void apply_window(func fn, JNIEnv* env, jfloatArray jf, unsigned int n) {
	float* rf = (float*) (*env)->GetPrimitiveArrayCritical(env, jf, 0);
	fn(rf, n);
	(*env)->ReleasePrimitiveArrayCritical(env, jf, rf, 0);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowRectangular
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_rectangular, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowTriangular
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_triangular, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowWelch
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_welch, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowHanning
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_hanning, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowHamming
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_hamming, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowBlackman
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_blackman, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowNuttall
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_nuttall, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowBlackmanNuttall
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_blackman_nuttall, env, jf, N);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_windowBlackmanHarris
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {
	apply_window(window_blackman_harris, env, jf, N);
}

// jfloat completely buggy when sent as argument!!!!
// swithed to jfloatArray with one-float size... shame on jni...
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_getMeanFrequency
	(JNIEnv* env, jobject obj, jfloatArray io, jfloatArray m, jint N ) {

	jfloat* jm = (*env)->GetFloatArrayElements(env, m, NULL);
	jfloat* jio = (*env)->GetFloatArrayElements(env, io, NULL);
	get_mean_frequency((float*) jio, (float*) jm, N);
	(*env)->ReleaseFloatArrayElements(env, m, jm, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, io, jio, JNI_ABORT);
}


// Data management
////////////////////
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_floatToShort
	(JNIEnv* env, jobject obj, jfloatArray jf, jshortArray js, jint N ) {

	short* rs = (short*) (*env)->GetPrimitiveArrayCritical(env, js, 0);
	float* rf = (float*) (*env)->GetPrimitiveArrayCritical(env, jf, 0);
	float_to_short(rs, rf, N);
	(*env)->ReleasePrimitiveArrayCritical(env, js, rs, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, jf, rf, 0);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_shortToFloat
	(JNIEnv* env, jobject obj, jshortArray js, jfloatArray jf, jint N ) {

	short* rs = (short*) (*env)->GetPrimitiveArrayCritical(env, js, 0);
	float* rf = (float*) (*env)->GetPrimitiveArrayCritical(env, jf, 0);
	short_to_float(rs, rf, N);
	(*env)->ReleasePrimitiveArrayCritical(env, js, rs, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, jf, rf, 0);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_clearShort
	(JNIEnv* env, jobject obj, jshortArray js, jint N ) {

	short* rs = (short*) (*env)->GetPrimitiveArrayCritical(env, js, 0);
	clear_short(rs, N);
	(*env)->ReleasePrimitiveArrayCritical(env, js, rs, 0);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_clearFloat
	(JNIEnv* env, jobject obj, jfloatArray jf, jint N ) {

	float* rf = (float*) (*env)->GetPrimitiveArrayCritical(env, jf, 0);
	clear_float(rf, N);
	(*env)->ReleasePrimitiveArrayCritical(env, jf, rf, 0);
}


// Conversion
///////////////
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_toPolar
	(JNIEnv* env, jobject obj, jfloatArray jre, jfloatArray jim, jint N ) {

	float* rre = (float*) (*env)->GetPrimitiveArrayCritical(env, jre, 0);
	float* rim = (float*) (*env)->GetPrimitiveArrayCritical(env, jim, 0);
	to_polar(rre, rim, N);
	(*env)->ReleasePrimitiveArrayCritical(env, jre, rre, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, jim, rim, 0);
}
JNIEXPORT void JNICALL Java_net_galmiza_android_engine_sound_SoundEngine_toCartesian
	(JNIEnv* env, jobject obj, jfloatArray jre, jfloatArray jim, jint N ) {

	float* rre = (float*) (*env)->GetPrimitiveArrayCritical(env, jre, 0);
	float* rim = (float*) (*env)->GetPrimitiveArrayCritical(env, jim, 0);
	to_cartesian(rre, rim, N);
	(*env)->ReleasePrimitiveArrayCritical(env, jre, rre, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, jim, rim, 0);
}
