#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

static void display_ps(char * s, pid_t pid) {
//char * s;
//pid_t pid;
// {
  (void) printf("Process %ld %s\n", pid, s);
}

int main (int argc, char ** argv) {
  pid_t procid;
  pid_t id;
  char * var = "Common data ";

  switch (id = fork()) {
    case -1 :
      perror ("Problem creating child");
      exit(2);
    case 0:
      procid = getpid();
      display_ps(var, procid);
      var = "Child";
      break;
   default :
     fprintf(stdout, "Value of Child process is %ld\n", id);
     procid = getpid();
     display_ps(var, procid);
     var = "Parent";
     break;
  }
  display_ps(var, procid);
  return 0;
}
