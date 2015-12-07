#!/bin/sh

# define vars
PROJECT_PATH=$(pwd)

# set the ndk folder as active directory
cd $ANDROID_NDK

# compile!
./ndk-build -C $PROJECT_PATH NDK_DEBUG=0 APP_ABI=all NDK_PROJECT_PATH=$PROJECT_PATH NDK_APPLICATION_MK=$PROJECT_PATH/jni/Android.mk 2>$PROJECT_PATH/jni/error.log
