# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

# FFmpeg library
include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeg
LOCAL_SRC_FILES := libffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)



include $(CLEAR_VARS)

LOCAL_MODULE    := ffmpegutils
LOCAL_SRC_FILES := hello-jni.c
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include

MY_LOG_TAG := \"uuuuu\"
#定义基于构建类型的默认日志等级
ifeq ($(APP_OPTIM),release)
	MY_LOG_LEVEL := MY_LOG_LEVEL_ERROR
else
	MY_LOG_LEVEL := MY_LOG_LEVEL_VERBOSE
endif
#追加编译标记
LOCAL_CFLAGS += -DMY_LOG_TAG=$(MY_LOG_TAG)
LOCAL_CFLAGS += -DMY_LOG_LEVEL=$(MY_LOG_LEVEL)
LOCAL_LDLIBS := -llog


LOCAL_SHARED_LIBRARIES := ffmpeg
LOCAL_SHARED_LIBRARIES += SDL2
LOCAL_PROGUARD_ENABLED:= disabled

include $(BUILD_SHARED_LIBRARY)
