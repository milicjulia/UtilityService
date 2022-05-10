LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := zip4j:libs/zip4j-1.3.2.jar

include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := UtilityService

LOCAL_JAVA_LIBRARIES += droidlogic droidlogic.external.pppoe

LOCAL_STATIC_JAVA_LIBRARIES += \
    com.iwedia.utilityservicecomm \
    zip4j

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_CERTIFICATE := platform

LOCAL_REQUIRED_MODULES := libUtilityServiceJNI

include $(BUILD_PACKAGE)
