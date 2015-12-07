// Our dependencies
#include <stdlib.h>
#include <math.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

// Android logs
#define LOG_TAG    "jni"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

// Constants
#define Pi 3.14159265358979f
#define N_WAVE          (64*1024)	/* dimension of fsin[] */
#define LOG2_N_WAVE     (6+10)		/* log2(N_WAVE) */
#define abs(x) ((x)>0?(x):-(x))

// Array initialized with pre calculated sin values
float fsin[N_WAVE];					/* populated at runtime */


// !!!!!!!!!!!!! WARNING SCALAR FLOATS FAIL TO BE PASSED AS PARAMETERS !!!!!!!!!!!!


/*
 * Populates the fsin array
 * Build the sin signal used for trigonometry calculations
 */
void init_fsin(void) {
	for (int i=0; i<N_WAVE; i++)
		fsin[i] = sinf(2*Pi/N_WAVE*i); // calculated once, no need to optimize
}

/*
 * Converts between time and frequency bases
 * Performs forward (inverse=0) and back (inverse=1) fft
 */
int fft(float* fr, float* fi, unsigned int m, int inverse) {
        int mr,nn,i,j,l,k,istep,n,scale,shift;
        float qr,qi,tr,ti,wr,wi,t;

        n = 1<<m; // n=2^m

        if(n > N_WAVE)
                return -1;

        mr = 0;
        nn = n - 1;
        scale = 0;

        /* decimation in time - re-order data */
        for(m=1; m<=nn; ++m) {
                l = n;
                do {
                        l >>= 1;
                } while(mr+l > nn);
                mr = (mr & (l-1)) + l;

                if(mr <= m) continue;
                tr = fr[m];
                fr[m] = fr[mr];
                fr[mr] = tr;
                ti = fi[m];
                fi[m] = fi[mr];
                fi[mr] = ti;
        }

        l = 1;
        k = LOG2_N_WAVE-1;
        while(l < n) {
                if(inverse) {
                        /* variable scaling, depending upon data */
                        shift = 0;
                        for(i=0; i<n; ++i) {
                                j = fr[i];
                                if(j < 0)
                                        j = -j;
                                m = fi[i];
                                if(m < 0)
                                        m = -m;
                        }
                        if(shift)
                                ++scale;
                } else {
                        /* fixed scaling, for proper normalization -
                           there will be log2(n) passes, so this
                           results in an overall factor of 1/n,
                           distributed to maximize arithmetic accuracy. */
                        shift = 1;
                }
                /* it may not be obvious, but the shift will be performed
                   on each data point exactly once, during this pass. */
                istep = l << 1;
                float h = 0.5f;
                for(m=0; m<l; ++m) {
                        j = m << k;
                        /* 0 <= j < N_WAVE/2 */
                        wr =  fsin[j+N_WAVE/4];
                        wi = -fsin[j];
                        if(inverse)
                                wi = -wi;
                        if(shift) {
                                wr *= h;
                                wi *= h;
                        }
                        for(i=m; i<n; i+=istep) {
                                j = i + l;
                                        tr = wr*fr[j]-wi*fi[j];
                                        ti = wr*fi[j]+wi*fr[j];
                                qr = fr[i];
                                qi = fi[i];
                                if(shift) {
                                        qr *= h;
                                        qi *= h;
                                }
                                fr[j] = qr - tr;
                                fi[j] = qi - ti;
                                fr[i] = qr + tr;
                                fi[i] = qi + ti;
                        }
                }
                --k;
                l = istep;
        }

        return scale;
}

/*
 * Window functions there (algorithms from Wikipedia!)
 */
void window_rectangular(float* fr, unsigned int n) {}
void window_triangular(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 1.0f-abs((2.0f*i-1.0f)/n-1.0f);
}
void window_welch(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j) {
		float r = 2.0f*i/(n+1)-1;
	    fr[i] *= 1.0f-r*r;
	}
}
void window_blackman(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 0.42659f-0.49656f*fsin[k%N_WAVE]+0.076849f*fsin[(2*k)%N_WAVE];
}
void window_nuttall(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 0.355768f-0.487396f*fsin[k%N_WAVE]+0.144232f*fsin[(2*k)%N_WAVE]-0.012604f*fsin[(3*k)%N_WAVE];
}
void window_blackman_nuttall(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 0.3635819f-0.4891775f*fsin[k%N_WAVE]+0.1369955f*fsin[(2*k)%N_WAVE]-0.0106411f*fsin[(3*k)%N_WAVE];
}
void window_blackman_harris(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 0.35875f-0.48829f*fsin[k%N_WAVE]+0.14128f*fsin[(2*k)%N_WAVE]-0.01168f*fsin[(3*k)%N_WAVE];
}
void window_hanning(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 0.5f-0.5f*fsin[k%N_WAVE];
}
void window_hamming(float* fr, unsigned int n) {
	int j = N_WAVE/n;
	for(int i=0, k=N_WAVE/4; i<n; ++i,k+=j)
	    fr[i] *= 0.54f-0.46f*fsin[k%N_WAVE];
}

/*
 * Mean frequency
 * f = sum(i*magnitude_i)/sum(magnitude_i)
 */
void get_mean_frequency(float* io, float* m, int n) {
	float sampling = io[0];
	float s1 = 0.0f;
	float s2 = 0.0f;
	for (int i=0; i<n; i++) {
		s1 += i*m[i];
		s2 += m[i];
	}
	io[0] = s1/s2*sampling/n;
}

/*
 * Convertions between cartesian and polar bases
 */
void to_polar(float* re, float* im, int n) {
	float re_i, im_i;
	for (int i=0; i<n; i++) {
		re_i = re[i];
		im_i = im[i];
		re[i] = sqrtf((re_i*re_i)+(im_i*im_i));
		im[i] = atan2(im_i, re_i);
	}
}
void to_cartesian(float* re, float* im, int n) {
	float re_i, im_i;
	unsigned int i;//,j,k;
	//j = N_WAVE/n;
	for (i=0/*,k=0*/; i<n; i++/*,k+=j*/) {
		re_i = re[i];
		im_i = im[i];
		re[i] = re_i*cosf(im_i);
		im[i] = re_i*sinf(im_i);
	}
}

/*
 * Convert float to short and short to float
 */
void short_to_float(short* s, float* f, int n) {
	float m = 32767.0f;
	for (int i=0; i<n; i++) {
		f[i] = (float)(s[i])/m;
	}
}
void float_to_short(float* f, short* s, int n) {
	float m = 32767;
	for (int i=0; i<n; i++) {
		s[i] = (short)(f[i]*m);
	}
}

/*
 * Fill arrays with zeros using fast memset function
 */
void clear_short(short* s, int n) {
	memset(s, 0, n*sizeof(short));
}
void clear_float(float* f, int n) {
	memset(f, 0, n*sizeof(float));
}
