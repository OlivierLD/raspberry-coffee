#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

int nativeDebugEnabled() {
  char * nativeDebug = "NATIVEDEBUG";
  int debug = FALSE;
  if (getenv(nativeDebug)) {
    if (strcmp("true", getenv(nativeDebug)) == 0) {
      debug = TRUE;
    }
  }
  return debug;
}

int main (int argc, char ** argv) {
  char * user  = "USER";
  char * shell = "SHELL";

  fprintf(stdout, "User:%s, Shell:%s\n", getenv(user), getenv(shell));

  char * olivDebug = "OLIVDEBUG";
  int debug = FALSE;
  if (getenv(olivDebug)) {
    if (strcmp("true", getenv(olivDebug)) == 0) {
      debug = TRUE;
    }
  }
  fprintf(stdout, "%s is %sset.\n", olivDebug, (debug ? "" : "not "));

  fprintf(stdout, "NATIVEDEBUG is %sset\n", (nativeDebugEnabled() ? "" : "not "));

  return 0;
}
