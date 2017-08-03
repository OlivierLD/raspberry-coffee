#include <jni.h>
#include <stdio.h>
#include "jnisample_HelloWorld.h"

JNIEXPORT void JNICALL Java_jnisample_HelloWorld_print (JNIEnv * env, jobject obj) {
  printf("Hello C World!\n");
  return;
}

/* For accessing primitive types from class use
           following field descriptors

           +---+---------+
           | Z | boolean |
           | B | byte    |
           | C | char    |
           | S | short   |
           | I | int     |
           | J | long    |
           | F | float   |
           | D | double  |
           +-------------+
*/
JNIEXPORT jint JNICALL Java_jnisample_HelloWorld_manageObject (JNIEnv * env, jobject obj1, jobject obj2) {
  printf("C receiving Java Object\n");
  jclass objClass = (*env)->GetObjectClass(env, obj2);
  jfieldID fidInt    = (*env)->GetFieldID(env, objClass, "someNumber", "I");
  jint iVal = (*env)->GetIntField(env, obj2, fidInt);
  printf("- Value: %d\n", iVal);

  jfieldID fidString = (*env)->GetFieldID(env, objClass, "name", "Ljava/lang/String;");
  jobject sName = (*env)->GetObjectField(env, obj2, fidString);
  const char *c_str;
  c_str = (*env)->GetStringUTFChars(env, sName, NULL);
  if (c_str != NULL) {
    printf("- Name: %s\n", c_str);
  }
  return 0;
}