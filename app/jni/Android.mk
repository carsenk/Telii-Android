JNI_DIR := $(call my-dir)
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE     := native-utils
LOCAL_C_INCLUDES := $(JNI_DIR)/utils/
LOCAL_CFLAGS     += -Wall

LOCAL_SRC_FILES := $(JNI_DIR)/utils/org_denarius_telii_util_FileUtils.cpp

include $(BUILD_SHARED_LIBRARY)