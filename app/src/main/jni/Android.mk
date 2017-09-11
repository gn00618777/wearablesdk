LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)



LOCAL_SRC_FILES := cwmWearable.c

LOCAL_LDLIBS += -llog

LOCAL_MODULE := wearable

LOCAL_CFLAGS :=

include $(BUILD_SHARED_LIBRARY)