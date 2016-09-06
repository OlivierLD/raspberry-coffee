#include <jni.h>
#include <stdio.h>
#include "jnisample_HelloWorld.h"

JNIEXPORT void JNICALL Java_jnisample_HelloWorld_print (JNIEnv * env, jobject obj)
{
  printf("Hello C World!\n");
  return;
}

