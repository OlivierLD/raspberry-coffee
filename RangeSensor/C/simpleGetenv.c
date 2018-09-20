#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

int nativeDebugEnabled() {
  const char * nativeDebug = getenv("NATIVEDEBUG");
  int debug = FALSE;
  if (nativeDebug != NULL) {
    fprintf(stdout, "==> %s\n", nativeDebug);
    if (strcmp("true", nativeDebug) == 0) {
      debug = TRUE;
    }
  } else {
    fprintf(stdout, "NATIVEDEBUG not set\n");
  }
  fprintf(stdout, "NATIVEDEBUG is %s\n", (debug ? "true" : "false"));
  return debug;
}

int main() {
  if (nativeDebugEnabled()) {
    fprintf(stdout, "Yes!!");
  } else {
    fprintf(stdout, "Nope.");
  }
  return 0;
}
