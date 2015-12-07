LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog -landroid 
LOCAL_MODULE := sound-engine
LOCAL_SRC_FILES := main.c jni.c
LOCAL_CFLAGS := -std=c99
APP_OPTIM := release

include $(BUILD_SHARED_LIBRARY)
