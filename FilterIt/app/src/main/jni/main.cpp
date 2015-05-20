#include <jni.h>
#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "JNIDebug"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#endif

#include <cmath>
#include "com_shenghangao_filterit_MainActivity.h"
extern "C" {

    JNIEXPORT jdoubleArray JNICALL Java_com_shenghangao_filterit_MainActivity_sineWave (JNIEnv *env, jobject jObj, jdouble width, jdouble height,jdouble amplitude, jdouble frequency)
    {
        LOGD("Sine wave function is called.");
        __android_log_print(ANDROID_LOG_DEBUG, "JNIDebug", "Another way: Sine wave function is called.");
        jdoubleArray result = env->NewDoubleArray(1005);
        jdouble y[1005];
        jdouble interval = 0.001;

        for (int i=0; i<1005; ++i)
        {
            y[i] = amplitude/100 * height/2 * sin(2*M_PI*frequency*i*interval);
        }

        env->SetDoubleArrayRegion(result, 0, 1004, y);

        return result;
    }

    JNIEXPORT jdoubleArray JNICALL Java_com_shenghangao_filterit_MainActivity_cosineWave (JNIEnv *env, jobject jObj, jdouble width, jdouble height,jdouble amplitude, jdouble frequency)
    {
        jdoubleArray result = env->NewDoubleArray(1005);
        jdouble y[1005];
        jdouble interval = 0.001;

        for (int i=0; i<1005; ++i)
        {
            y[i] = amplitude/100 * height/2 * cos(2*M_PI*frequency*i*interval);
        }

        env->SetDoubleArrayRegion(result, 0, 1004, y);

        return result;
    }

    JNIEXPORT jdoubleArray JNICALL Java_com_shenghangao_filterit_MainActivity_LPF (JNIEnv *env, jobject jObj, jdoubleArray lpfin, jdouble factor)
    {
            int len = env->GetArrayLength(lpfin);
            jdoubleArray result = env->NewDoubleArray(len);
            jdouble y[len];


            jdouble *input = env->GetDoubleArrayElements(lpfin, 0);
            y[0] = input[0];
            for (int i=1; i<len; ++i)
            {
                y[i] = factor * input[i] + (1-factor) * y[i-1];
            }

            env->SetDoubleArrayRegion(result, 0, len-1, y);
            env->ReleaseDoubleArrayElements(lpfin, input, 0);
            return result;
    }

}
