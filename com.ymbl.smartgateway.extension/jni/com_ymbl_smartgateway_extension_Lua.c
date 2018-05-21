#include <jni.h>
#include "dlog.h"

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_Lua_exec
  (JNIEnv *jenv, jobject jobj, jstring cmd)
{
	AT_PRINTF("[JNI] %s(): no functions in here\n", __func__);
}
